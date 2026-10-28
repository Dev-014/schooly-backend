package com.school.erp.dto.notification;

import com.school.erp.entity.notification.Notification;
import com.school.erp.entity.notification.NotificationCategory;
import com.school.erp.entity.notification.NotificationPriority;
import com.school.erp.entity.notification.NotificationType;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;

public record NotificationResponse(
        Long id,
        Long schoolId,
        Long userId,
        String targetRole,
        String title,
        String message,
        NotificationType notificationType,
        NotificationCategory category,
        NotificationPriority priority,
        Boolean isRead,
        LocalDateTime readAt,
        Long actorId,
        String actorRole,
        String actorName,
        String actionUrl,
        String actionType,
        String entityType,
        Long entityId,
        Map<String, Object> metadata,
        LocalDateTime createdAt,
        String relativeTime
) {
    public static NotificationResponse fromEntity(Notification entity) {
        if (entity == null) {
            return null;
        }
        return new NotificationResponse(
                entity.getId(),
                entity.getSchoolId(),
                entity.getUserId(),
                entity.getTargetRole(),
                entity.getTitle(),
                entity.getMessage(),
                entity.getNotificationType(),
                entity.getCategory(),
                entity.getPriority(),
                entity.getIsRead(),
                entity.getReadAt(),
                entity.getActorId(),
                entity.getActorRole(),
                entity.getActorName(),
                entity.getActionUrl(),
                entity.getActionType(),
                entity.getEntityType(),
                entity.getEntityId(),
                entity.getMetadata(),
                entity.getCreatedAt(),
                calculateRelativeTime(entity.getCreatedAt())
        );
    }

    private static String calculateRelativeTime(LocalDateTime createdAt) {
        if (createdAt == null) {
            return "just now";
        }
        Duration diff = Duration.between(createdAt, LocalDateTime.now());
        long seconds = diff.getSeconds();
        if (seconds < 60) {
            return "just now";
        }
        long minutes = diff.toMinutes();
        if (minutes < 60) {
            return minutes + "m ago";
        }
        long hours = diff.toHours();
        if (hours < 24) {
            return hours + "h ago";
        }
        long days = diff.toDays();
        if (days < 7) {
            return days + "d ago";
        }
        return createdAt.toLocalDate().toString();
    }
}
