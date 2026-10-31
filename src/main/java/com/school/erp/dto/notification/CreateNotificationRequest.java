package com.school.erp.dto.notification;

import com.school.erp.entity.notification.NotificationCategory;
import com.school.erp.entity.notification.NotificationPriority;
import com.school.erp.entity.notification.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Map;

public record CreateNotificationRequest(
        Long userId, // Optional: if null, broadcast to targetRole
        @NotBlank(message = "Target role is required")
        String targetRole,
        @NotBlank(message = "Title is required")
        @Size(max = 255, message = "Title must not exceed 255 characters")
        String title,
        @NotBlank(message = "Message is required")
        String message,
        @NotNull(message = "Notification type is required")
        NotificationType notificationType,
        NotificationCategory category,
        NotificationPriority priority,
        String actionUrl,
        String actionType,
        String entityType,
        Long entityId,
        Map<String, Object> metadata
) {
}
