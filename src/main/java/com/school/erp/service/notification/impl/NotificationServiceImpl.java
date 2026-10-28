package com.school.erp.service.notification.impl;

import com.school.erp.dto.notification.CreateNotificationRequest;
import com.school.erp.dto.notification.NotificationCountResponse;
import com.school.erp.dto.notification.NotificationResponse;
import com.school.erp.entity.auth.User;
import com.school.erp.entity.auth.UserRole;
import com.school.erp.entity.auth.UserSchoolRole;
import com.school.erp.entity.notification.Notification;
import com.school.erp.entity.notification.NotificationCategory;
import com.school.erp.entity.notification.NotificationPriority;
import com.school.erp.entity.notification.NotificationType;
import com.school.erp.exception.ResourceNotFoundException;
import com.school.erp.repository.auth.UserRepository;
import com.school.erp.repository.auth.UserSchoolRoleRepository;
import com.school.erp.repository.notification.NotificationRepository;
import com.school.erp.security.AuthenticatedUser;
import com.school.erp.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserSchoolRoleRepository userSchoolRoleRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationResponse> getNotifications(
            Long userId,
            Long schoolId,
            Boolean isRead,
            NotificationCategory category,
            NotificationType notificationType,
            NotificationPriority priority,
            Pageable pageable
    ) {
        Page<Notification> page = notificationRepository.filterNotifications(
                schoolId, userId, isRead, category, notificationType, priority, pageable
        );
        return page.map(NotificationResponse::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationCountResponse getUnreadSummary(Long userId, Long schoolId) {
        long unreadCount = notificationRepository.countBySchoolIdAndUserIdAndIsReadFalse(schoolId, userId);
        long totalCount = notificationRepository.countBySchoolIdAndUserId(schoolId, userId);

        List<Object[]> categoryCounts = notificationRepository.countUnreadGroupedByCategory(schoolId, userId);
        Map<String, Long> unreadByCategory = new HashMap<>();
        for (Object[] row : categoryCounts) {
            if (row[0] != null && row[1] != null) {
                unreadByCategory.put(row[0].toString(), (Long) row[1]);
            }
        }

        return new NotificationCountResponse(unreadCount, totalCount, unreadByCategory);
    }

    @Override
    @Transactional
    public NotificationResponse markAsRead(Long id, Long userId, Long schoolId) {
        Notification notification = notificationRepository.findByIdAndSchoolIdAndUserId(id, schoolId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + id));

        if (!Boolean.TRUE.equals(notification.getIsRead())) {
            notification.setIsRead(true);
            notification.setReadAt(LocalDateTime.now());
            notification = notificationRepository.save(notification);
        }

        return NotificationResponse.fromEntity(notification);
    }

    @Override
    @Transactional
    public int markAllAsRead(Long userId, Long schoolId) {
        return notificationRepository.markAllAsRead(schoolId, userId, LocalDateTime.now());
    }

    @Override
    @Transactional
    public int markBatchAsRead(List<Long> ids, Long userId, Long schoolId) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }
        return notificationRepository.markBatchAsRead(schoolId, userId, ids, LocalDateTime.now());
    }

    @Override
    @Transactional
    public void deleteNotification(Long id, Long userId, Long schoolId) {
        Notification notification = notificationRepository.findByIdAndSchoolIdAndUserId(id, schoolId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + id));
        notificationRepository.delete(notification);
    }

    @Override
    @Transactional
    public int clearReadNotifications(Long userId, Long schoolId) {
        return notificationRepository.deleteReadNotifications(schoolId, userId);
    }

    @Override
    @Transactional
    public List<NotificationResponse> createNotification(
            CreateNotificationRequest request,
            Long schoolId,
            AuthenticatedUser currentUser
    ) {
        NotificationCategory resolvedCategory = request.category() != null
                ? request.category()
                : resolveCategoryFromType(request.notificationType());

        NotificationPriority resolvedPriority = request.priority() != null
                ? request.priority()
                : NotificationPriority.NORMAL;

        Long actorId = currentUser != null ? currentUser.userId() : null;
        String actorRole = currentUser != null && currentUser.role() != null ? currentUser.role().name() : "SYSTEM";
        String actorName = resolveActorName(actorId, actorRole);

        // Case 1: Targeted to a single user
        if (request.userId() != null) {
            Notification saved = sendDirectNotification(
                    schoolId,
                    request.userId(),
                    request.targetRole(),
                    request.notificationType(),
                    resolvedCategory,
                    resolvedPriority,
                    request.title(),
                    request.message(),
                    actorId,
                    actorRole,
                    actorName,
                    request.actionUrl(),
                    request.actionType(),
                    request.entityType(),
                    request.entityId(),
                    request.metadata()
            );
            return List.of(NotificationResponse.fromEntity(saved));
        }

        // Case 2: Broadcast to all active users with targetRole in school
        List<Notification> created = broadcastToRole(
                schoolId,
                request.targetRole(),
                request.notificationType(),
                resolvedCategory,
                resolvedPriority,
                request.title(),
                request.message(),
                actorId,
                actorRole,
                actorName,
                request.actionUrl(),
                request.actionType(),
                request.entityType(),
                request.entityId(),
                request.metadata()
        );

        return created.stream()
                .map(NotificationResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Notification sendDirectNotification(
            Long schoolId,
            Long recipientUserId,
            String targetRole,
            NotificationType notificationType,
            NotificationCategory category,
            NotificationPriority priority,
            String title,
            String message,
            Long actorId,
            String actorRole,
            String actorName,
            String actionUrl,
            String actionType,
            String entityType,
            Long entityId,
            Map<String, Object> metadata
    ) {
        Notification notification = Notification.builder()
                .schoolId(schoolId)
                .userId(recipientUserId)
                .targetRole(targetRole != null ? targetRole.toUpperCase() : "GENERAL")
                .title(title)
                .message(message)
                .notificationType(notificationType)
                .category(category != null ? category : resolveCategoryFromType(notificationType))
                .priority(priority != null ? priority : NotificationPriority.NORMAL)
                .isRead(false)
                .actorId(actorId)
                .actorRole(actorRole)
                .actorName(actorName)
                .actionUrl(actionUrl)
                .actionType(actionType != null ? actionType : "NAVIGATE")
                .entityType(entityType)
                .entityId(entityId)
                .metadata(metadata != null ? new HashMap<>(metadata) : new HashMap<>())
                .build();

        Notification saved = notificationRepository.save(notification);
        log.info("Dispatched notification [{}] to user [{}] for school [{}]", notificationType, recipientUserId, schoolId);
        return saved;
    }

    @Override
    @Transactional
    public List<Notification> broadcastToRole(
            Long schoolId,
            String targetRole,
            NotificationType notificationType,
            NotificationCategory category,
            NotificationPriority priority,
            String title,
            String message,
            Long actorId,
            String actorRole,
            String actorName,
            String actionUrl,
            String actionType,
            String entityType,
            Long entityId,
            Map<String, Object> metadata
    ) {
        String roleStr = targetRole != null ? targetRole.toUpperCase().trim() : "ADMIN";
        UserRole userRole;
        try {
            userRole = UserRole.valueOf(roleStr);
        } catch (IllegalArgumentException e) {
            userRole = UserRole.ADMIN;
        }

        List<UserSchoolRole> activeSchoolRoles = userSchoolRoleRepository
                .findBySchoolIdAndRoleAndStatusIgnoreCase(schoolId, userRole, "ACTIVE");

        Set<Long> userIds = activeSchoolRoles.stream()
                .map(usr -> usr.getUser().getId())
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (userIds.isEmpty()) {
            log.warn("No active users found for role [{}] in school [{}], skipping broadcast", roleStr, schoolId);
            return Collections.emptyList();
        }

        NotificationCategory cat = category != null ? category : resolveCategoryFromType(notificationType);
        NotificationPriority prio = priority != null ? priority : NotificationPriority.NORMAL;

        List<Notification> toSave = userIds.stream()
                .map(uid -> Notification.builder()
                        .schoolId(schoolId)
                        .userId(uid)
                        .targetRole(roleStr)
                        .title(title)
                        .message(message)
                        .notificationType(notificationType)
                        .category(cat)
                        .priority(prio)
                        .isRead(false)
                        .actorId(actorId)
                        .actorRole(actorRole)
                        .actorName(actorName)
                        .actionUrl(actionUrl)
                        .actionType(actionType != null ? actionType : "NAVIGATE")
                        .entityType(entityType)
                        .entityId(entityId)
                        .metadata(metadata != null ? new HashMap<>(metadata) : new HashMap<>())
                        .build())
                .collect(Collectors.toList());

        List<Notification> saved = notificationRepository.saveAll(toSave);
        log.info("Broadcasted notification [{}] to {} users in role [{}] for school [{}]",
                notificationType, saved.size(), roleStr, schoolId);
        return saved;
    }

    private NotificationCategory resolveCategoryFromType(NotificationType type) {
        if (type == null) {
            return NotificationCategory.GENERAL;
        }
        return switch (type) {
            case HOMEWORK_ASSIGNED, HOMEWORK_SUBMITTED, HOMEWORK_GRADED,
                 EXAM_SCHEDULE_PUBLISHED, EXAM_ADMIT_CARD_READY, EXAM_RESULT_PUBLISHED -> NotificationCategory.ACADEMIC;
            case ATTENDANCE_ABSENT, ATTENDANCE_LATE, ATTENDANCE_LOW_WARNING -> NotificationCategory.ATTENDANCE;
            case FEE_INVOICE_GENERATED, FEE_DUE_REMINDER, FEE_PAYMENT_SUCCESS, FEE_OVERDUE_ALERT -> NotificationCategory.FINANCE;
            case LEAVE_REQUESTED, LEAVE_STATUS_UPDATED, PAYROLL_GENERATED, STAFF_TASK_ASSIGNED -> NotificationCategory.HR;
            case VISITOR_CHECKED_IN, GATE_PASS_ISSUED, PARCEL_RECEIVED, COMPLAINT_FILED, COMPLAINT_RESOLVED -> NotificationCategory.FRONTOFFICE;
            case BOOK_ISSUED, BOOK_DUE_REMINDER, BOOK_OVERDUE_ALERT -> NotificationCategory.LIBRARY;
            case CIRCULAR_ANNOUNCEMENT, EMERGENCY_ALERT -> NotificationCategory.COMMUNICATION;
            case SYSTEM_NOTIFICATION -> NotificationCategory.SYSTEM;
        };
    }

    private String resolveActorName(Long actorId, String defaultRole) {
        if (actorId == null) {
            return "System Notification";
        }
        return userRepository.findById(actorId)
                .map(User::getName)
                .filter(name -> name != null && !name.isBlank())
                .orElse(defaultRole);
    }
}
