package com.school.erp.event.notification;

import com.school.erp.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventListener {

    private final NotificationService notificationService;

    @EventListener
    public void onNotificationEvent(NotificationEvent event) {
        try {
            if (event.getRecipientUserId() != null) {
                notificationService.sendDirectNotification(
                        event.getSchoolId(),
                        event.getRecipientUserId(),
                        event.getTargetRole(),
                        event.getNotificationType(),
                        event.getCategory(),
                        event.getPriority(),
                        event.getTitle(),
                        event.getMessage(),
                        event.getActorId(),
                        event.getActorRole(),
                        event.getActorName(),
                        event.getActionUrl(),
                        event.getActionType(),
                        event.getEntityType(),
                        event.getEntityId(),
                        event.getMetadata()
                );
            } else if (event.getTargetRole() != null) {
                notificationService.broadcastToRole(
                        event.getSchoolId(),
                        event.getTargetRole(),
                        event.getNotificationType(),
                        event.getCategory(),
                        event.getPriority(),
                        event.getTitle(),
                        event.getMessage(),
                        event.getActorId(),
                        event.getActorRole(),
                        event.getActorName(),
                        event.getActionUrl(),
                        event.getActionType(),
                        event.getEntityType(),
                        event.getEntityId(),
                        event.getMetadata()
                );
            }
        } catch (Exception e) {
            log.error("Failed to process notification event for type [{}]: {}", event.getNotificationType(), e.getMessage(), e);
        }
    }
}
