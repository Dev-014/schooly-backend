package com.school.erp.service;

import com.google.firebase.messaging.Message;
import com.school.erp.entity.notification.Notification;
import com.school.erp.entity.notification.NotificationCategory;
import com.school.erp.entity.notification.NotificationDeviceToken;
import com.school.erp.entity.notification.NotificationPriority;
import com.school.erp.entity.notification.NotificationType;
import com.school.erp.repository.notification.NotificationDeviceTokenRepository;
import com.school.erp.service.notification.FcmPushService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FcmPushServiceTest {

    @Mock
    NotificationDeviceTokenRepository deviceTokenRepository;

    @InjectMocks
    FcmPushService fcmPushService;

    @Test
    void registerDeviceTokenDeactivatesPreviousOwnerAndActivatesForCurrentUser() {
        NotificationDeviceToken oldOwner = NotificationDeviceToken.builder()
                .id(1L).userId(99L).schoolId(7L).token("fcm-token-1").active(true).build();
        when(deviceTokenRepository.findByToken("fcm-token-1")).thenReturn(List.of(oldOwner));
        when(deviceTokenRepository.findByUserIdAndToken(42L, "fcm-token-1")).thenReturn(Optional.empty());
        when(deviceTokenRepository.save(any())).thenAnswer(inv -> {
            NotificationDeviceToken t = inv.getArgument(0);
            if (t.getId() == null) t.setId(2L);
            return t;
        });

        NotificationDeviceToken saved = fcmPushService.registerDeviceToken(7L, 42L, "parent", " fcm-token-1 ", "android");

        assertFalse(oldOwner.getActive());
        assertEquals(2L, saved.getId());
        assertEquals(42L, saved.getUserId());
        assertEquals(7L, saved.getSchoolId());
        assertEquals("PARENT", saved.getTargetRole());
        assertEquals("ANDROID", saved.getPlatform());
        assertTrue(saved.getActive());
    }

    @Test
    void sendPushNotificationsIsSafeNoOpWhenFirebaseCredentialsNotConfigured() {
        Notification notification = Notification.builder()
                .id(10L).schoolId(7L).userId(42L).targetRole("STUDENT")
                .title("Homework").message("Math exercise 4.2")
                .notificationType(NotificationType.HOMEWORK_ASSIGNED)
                .category(NotificationCategory.ACADEMIC)
                .priority(NotificationPriority.HIGH)
                .build();

        assertFalse(fcmPushService.isFirebaseReady());
        assertDoesNotThrow(() -> fcmPushService.sendPushNotifications(List.of(notification)));
        verifyNoInteractions(deviceTokenRepository);
    }

    @Test
    void unregisterDeviceTokenDeactivatesTokenForUser() {
        when(deviceTokenRepository.deactivateToken(42L, "fcm-token-1")).thenReturn(1);
        int count = fcmPushService.unregisterDeviceToken(42L, " fcm-token-1 ");
        assertEquals(1, count);
        verify(deviceTokenRepository).deactivateToken(42L, "fcm-token-1");
    }
}
