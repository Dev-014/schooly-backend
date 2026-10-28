package com.school.erp.service.notification;

import com.school.erp.dto.notification.CreateNotificationRequest;
import com.school.erp.dto.notification.NotificationCountResponse;
import com.school.erp.dto.notification.NotificationResponse;
import com.school.erp.entity.notification.Notification;
import com.school.erp.entity.notification.NotificationCategory;
import com.school.erp.entity.notification.NotificationPriority;
import com.school.erp.entity.notification.NotificationType;
import com.school.erp.security.AuthenticatedUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface NotificationService {

    Page<NotificationResponse> getNotifications(
            Long userId,
            Long schoolId,
            Boolean isRead,
            NotificationCategory category,
            NotificationType notificationType,
            NotificationPriority priority,
            Pageable pageable
    );

    NotificationCountResponse getUnreadSummary(Long userId, Long schoolId);

    NotificationResponse markAsRead(Long id, Long userId, Long schoolId);

    int markAllAsRead(Long userId, Long schoolId);

    int markBatchAsRead(List<Long> ids, Long userId, Long schoolId);

    void deleteNotification(Long id, Long userId, Long schoolId);

    int clearReadNotifications(Long userId, Long schoolId);

    List<NotificationResponse> createNotification(
            CreateNotificationRequest request,
            Long schoolId,
            AuthenticatedUser currentUser
    );

    Notification sendDirectNotification(
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
    );

    List<Notification> broadcastToRole(
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
    );
}
