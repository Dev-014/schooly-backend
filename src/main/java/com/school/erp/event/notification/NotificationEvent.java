package com.school.erp.event.notification;

import com.school.erp.entity.notification.NotificationCategory;
import com.school.erp.entity.notification.NotificationPriority;
import com.school.erp.entity.notification.NotificationType;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.Map;

@Getter
public class NotificationEvent extends ApplicationEvent {

    private final Long schoolId;
    private final Long recipientUserId; // If null, broadcast to targetRole
    private final String targetRole;
    private final NotificationType notificationType;
    private final NotificationCategory category;
    private final NotificationPriority priority;
    private final String title;
    private final String message;
    private final Long actorId;
    private final String actorRole;
    private final String actorName;
    private final String actionUrl;
    private final String actionType;
    private final String entityType;
    private final Long entityId;
    private final Map<String, Object> metadata;

    public NotificationEvent(
            Object source,
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
        super(source);
        this.schoolId = schoolId;
        this.recipientUserId = recipientUserId;
        this.targetRole = targetRole;
        this.notificationType = notificationType;
        this.category = category;
        this.priority = priority;
        this.title = title;
        this.message = message;
        this.actorId = actorId;
        this.actorRole = actorRole;
        this.actorName = actorName;
        this.actionUrl = actionUrl;
        this.actionType = actionType;
        this.entityType = entityType;
        this.entityId = entityId;
        this.metadata = metadata;
    }
}
