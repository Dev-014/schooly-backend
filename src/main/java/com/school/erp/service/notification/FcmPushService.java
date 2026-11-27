package com.school.erp.service.notification;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.AndroidNotification;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MessagingErrorCode;
import com.school.erp.entity.notification.Notification;
import com.school.erp.entity.notification.NotificationDeviceToken;
import com.school.erp.repository.notification.NotificationDeviceTokenRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FcmPushService {

    public static final String ANDROID_CHANNEL_ID = "schooly_high_importance_channel";

    private final NotificationDeviceTokenRepository deviceTokenRepository;

    @Value("${FIREBASE_CREDENTIALS_JSON:}")
    private String firebaseCredentialsJson;

    @Value("${FIREBASE_CREDENTIALS_PATH:}")
    private String firebaseCredentialsPath;

    private volatile boolean firebaseReady = false;

    @PostConstruct
    public void initFirebase() {
        try {
            if (!FirebaseApp.getApps().isEmpty()) {
                firebaseReady = true;
                return;
            }
            InputStream credentialsStream = resolveCredentialsStream();
            if (credentialsStream == null) {
                log.info("Firebase credentials not configured; FCM push delivery is in no-op mode.");
                return;
            }
            try (InputStream is = credentialsStream) {
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(is))
                        .build();
                FirebaseApp.initializeApp(options);
                firebaseReady = true;
                log.info("Firebase Admin SDK initialized for FCM push notifications.");
            }
        } catch (Exception e) {
            firebaseReady = false;
            log.warn("Unable to initialize Firebase Admin SDK (push delivery disabled): {}", e.getMessage());
        }
    }

    private InputStream resolveCredentialsStream() throws Exception {
        if (firebaseCredentialsJson != null && !firebaseCredentialsJson.isBlank()) {
            return new ByteArrayInputStream(firebaseCredentialsJson.trim().getBytes(StandardCharsets.UTF_8));
        }
        if (firebaseCredentialsPath != null && !firebaseCredentialsPath.isBlank()) {
            return new FileInputStream(firebaseCredentialsPath.trim());
        }
        String googleAppCreds = System.getenv("GOOGLE_APPLICATION_CREDENTIALS");
        if (googleAppCreds != null && !googleAppCreds.isBlank()) {
            return new FileInputStream(googleAppCreds.trim());
        }
        return null;
    }

    public boolean isFirebaseReady() {
        return firebaseReady;
    }

    @Transactional
    public NotificationDeviceToken registerDeviceToken(
            Long schoolId,
            Long userId,
            String targetRole,
            String token,
            String platform
    ) {
        if (schoolId == null || userId == null || token == null || token.isBlank()) {
            throw new IllegalArgumentException("schoolId, userId, and token are required");
        }
        String normalizedToken = token.trim();
        String normalizedPlatform = (platform != null && !platform.isBlank())
                ? platform.trim().toUpperCase()
                : "ANDROID";
        String normalizedRole = (targetRole != null && !targetRole.isBlank())
                ? targetRole.trim().toUpperCase()
                : null;

        // Deactivate this token on any other user account on the same device
        for (NotificationDeviceToken existingOther : deviceTokenRepository.findByToken(normalizedToken)) {
            if (!userId.equals(existingOther.getUserId()) && Boolean.TRUE.equals(existingOther.getActive())) {
                existingOther.setActive(false);
                deviceTokenRepository.save(existingOther);
            }
        }

        NotificationDeviceToken entity = deviceTokenRepository.findByUserIdAndToken(userId, normalizedToken)
                .orElseGet(() -> NotificationDeviceToken.builder()
                        .userId(userId)
                        .token(normalizedToken)
                        .build());

        entity.setSchoolId(schoolId);
        entity.setTargetRole(normalizedRole);
        entity.setPlatform(normalizedPlatform);
        entity.setActive(true);
        entity.setLastSeenAt(LocalDateTime.now());
        return deviceTokenRepository.save(entity);
    }

    @Transactional
    public int unregisterDeviceToken(Long userId, String token) {
        if (userId == null || token == null || token.isBlank()) {
            return 0;
        }
        return deviceTokenRepository.deactivateToken(userId, token.trim());
    }

    @Async
    @Transactional
    public void sendPushNotifications(List<Notification> notifications) {
        if (!firebaseReady || notifications == null || notifications.isEmpty()) {
            return;
        }

        Map<Long, List<Notification>> bySchool = notifications.stream()
                .filter(n -> n != null && n.getSchoolId() != null && n.getUserId() != null)
                .collect(Collectors.groupingBy(Notification::getSchoolId));

        for (Map.Entry<Long, List<Notification>> entry : bySchool.entrySet()) {
            Long schoolId = entry.getKey();
            List<Notification> schoolNotifications = entry.getValue();
            Set<Long> userIds = schoolNotifications.stream()
                    .map(Notification::getUserId)
                    .collect(Collectors.toSet());

            List<NotificationDeviceToken> tokens = deviceTokenRepository
                    .findBySchoolIdAndUserIdInAndActiveTrue(schoolId, userIds);
            if (tokens.isEmpty()) {
                continue;
            }

            Map<Long, List<NotificationDeviceToken>> tokensByUser = tokens.stream()
                    .collect(Collectors.groupingBy(NotificationDeviceToken::getUserId));

            for (Notification notification : schoolNotifications) {
                List<NotificationDeviceToken> userTokens = tokensByUser.get(notification.getUserId());
                if (userTokens == null || userTokens.isEmpty()) {
                    continue;
                }
                for (NotificationDeviceToken deviceToken : userTokens) {
                    sendSinglePush(notification, deviceToken);
                }
            }
        }
    }

    protected void sendSinglePush(Notification notification, NotificationDeviceToken deviceToken) {
        try {
            Message message = buildMessage(notification, deviceToken.getToken());
            FirebaseMessaging.getInstance().send(message);
        } catch (FirebaseMessagingException e) {
            MessagingErrorCode code = e.getMessagingErrorCode();
            if (code == MessagingErrorCode.UNREGISTERED || code == MessagingErrorCode.INVALID_ARGUMENT) {
                log.info("Deactivating invalid FCM token id={} due to {}", deviceToken.getId(), code);
                deviceToken.setActive(false);
                deviceTokenRepository.save(deviceToken);
            } else {
                log.warn("FCM push delivery failed for notificationId={} tokenId={}: {}",
                        notification.getId(), deviceToken.getId(), e.getMessage());
            }
        } catch (Exception e) {
            log.warn("Unexpected error sending FCM push for notificationId={}: {}",
                    notification.getId(), e.getMessage());
        }
    }

    Message buildMessage(Notification notification, String token) {
        Map<String, String> data = new HashMap<>();
        if (notification.getId() != null) data.put("notificationId", notification.getId().toString());
        if (notification.getSchoolId() != null) data.put("schoolId", notification.getSchoolId().toString());
        if (notification.getTargetRole() != null) data.put("targetRole", notification.getTargetRole());
        if (notification.getNotificationType() != null) data.put("notificationType", notification.getNotificationType().name());
        if (notification.getCategory() != null) data.put("category", notification.getCategory().name());
        if (notification.getPriority() != null) data.put("priority", notification.getPriority().name());
        if (notification.getActionUrl() != null) data.put("actionUrl", notification.getActionUrl());
        if (notification.getActionType() != null) data.put("actionType", notification.getActionType());
        if (notification.getEntityType() != null) data.put("entityType", notification.getEntityType());
        if (notification.getEntityId() != null) data.put("entityId", notification.getEntityId().toString());

        AndroidConfig androidConfig = AndroidConfig.builder()
                .setPriority(AndroidConfig.Priority.HIGH)
                .setNotification(AndroidNotification.builder()
                        .setChannelId(ANDROID_CHANNEL_ID)
                        .setSound("default")
                        .build())
                .build();

        return Message.builder()
                .setToken(token)
                .setNotification(com.google.firebase.messaging.Notification.builder()
                        .setTitle(notification.getTitle())
                        .setBody(notification.getMessage())
                        .build())
                .setAndroidConfig(androidConfig)
                .putAllData(data)
                .build();
    }
}
