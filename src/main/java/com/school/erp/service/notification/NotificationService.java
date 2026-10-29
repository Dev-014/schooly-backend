package com.school.erp.service.notification;

import com.school.erp.dto.notification.CreateNotificationRequest;
import com.school.erp.dto.notification.NotificationCountResponse;
import com.school.erp.dto.notification.NotificationResponse;
import com.school.erp.entity.hr.Staff;
import com.school.erp.entity.notification.Notification;
import com.school.erp.entity.notification.NotificationCategory;
import com.school.erp.entity.notification.NotificationPriority;
import com.school.erp.entity.notification.NotificationType;
import com.school.erp.entity.student.Student;
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

    default Page<NotificationResponse> getNotifications(
            Long userId,
            Long schoolId,
            String targetRole,
            Boolean isRead,
            NotificationCategory category,
            NotificationType notificationType,
            NotificationPriority priority,
            Pageable pageable
    ) {
        return getNotifications(userId, schoolId, isRead, category, notificationType, priority, pageable);
    }

    NotificationCountResponse getUnreadSummary(Long userId, Long schoolId);

    default NotificationCountResponse getUnreadSummary(Long userId, Long schoolId, String targetRole) {
        return getUnreadSummary(userId, schoolId);
    }

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

    void notifyStudentAndParents(
            Long schoolId,
            Student student,
            NotificationType notificationType,
            NotificationPriority priority,
            String studentTitle,
            String studentMessage,
            String parentTitle,
            String parentMessage,
            String actionUrl,
            String entityType,
            Long entityId,
            Map<String, Object> metadata
    );

    default void notifyStudentAndParents(
            Long schoolId,
            Student student,
            NotificationType notificationType,
            NotificationPriority priority,
            String title,
            String message,
            String actionUrl,
            String actionLabel,
            String entityType,
            String entityId,
            Map<String, Object> metadata
    ) {
        notifyStudentAndParents(
                schoolId,
                student,
                notificationType,
                priority,
                title,
                message,
                title,
                message,
                actionUrl,
                entityType,
                parseEntityId(entityId),
                metadata
        );
    }

    void notifyClassAndSection(
            Long schoolId,
            Long classId,
            Long sectionId,
            boolean includeStudents,
            boolean includeParents,
            boolean includeTeachers,
            NotificationType notificationType,
            NotificationPriority priority,
            String title,
            String message,
            String actionUrl,
            String entityType,
            Long entityId,
            Map<String, Object> metadata
    );

    default void notifyClassAndSection(
            Long schoolId,
            Long classId,
            Long sectionId,
            boolean includeStudents,
            boolean includeParents,
            boolean includeTeachers,
            NotificationType notificationType,
            NotificationPriority priority,
            String title,
            String message,
            String actionUrl,
            String actionLabel,
            String entityType,
            String entityId,
            Map<String, Object> metadata
    ) {
        notifyClassAndSection(
                schoolId,
                classId,
                sectionId,
                includeStudents,
                includeParents,
                includeTeachers,
                notificationType,
                priority,
                title,
                message,
                actionUrl,
                entityType,
                parseEntityId(entityId),
                metadata
        );
    }

    void notifyClassTeachersOfStudent(
            Long schoolId,
            Student student,
            NotificationType notificationType,
            NotificationPriority priority,
            String title,
            String message,
            String actionUrl,
            String entityType,
            Long entityId,
            Map<String, Object> metadata
    );

    default void notifyClassTeachersOfStudent(
            Long schoolId,
            Student student,
            NotificationType notificationType,
            NotificationPriority priority,
            String title,
            String message,
            String actionUrl,
            String actionLabel,
            String entityType,
            String entityId,
            Map<String, Object> metadata
    ) {
        notifyClassTeachersOfStudent(
                schoolId,
                student,
                notificationType,
                priority,
                title,
                message,
                actionUrl,
                entityType,
                parseEntityId(entityId),
                metadata
        );
    }

    void notifyStaffMember(
            Long schoolId,
            Staff staff,
            NotificationType notificationType,
            NotificationPriority priority,
            String title,
            String message,
            String actionUrl,
            String entityType,
            Long entityId,
            Map<String, Object> metadata
    );

    default void notifyStaffMember(
            Long schoolId,
            Staff staff,
            NotificationType notificationType,
            NotificationPriority priority,
            String title,
            String message,
            String actionUrl,
            String actionLabel,
            String entityType,
            String entityId,
            Map<String, Object> metadata
    ) {
        notifyStaffMember(
                schoolId,
                staff,
                notificationType,
                priority,
                title,
                message,
                actionUrl,
                entityType,
                parseEntityId(entityId),
                metadata
        );
    }

    void notifySchoolAdmins(
            Long schoolId,
            NotificationType notificationType,
            NotificationPriority priority,
            String title,
            String message,
            String actionUrl,
            String entityType,
            Long entityId,
            Map<String, Object> metadata
    );

    default void notifySchoolAdmins(
            Long schoolId,
            NotificationType notificationType,
            NotificationPriority priority,
            String title,
            String message,
            String actionUrl,
            String actionLabel,
            String entityType,
            String entityId,
            Map<String, Object> metadata
    ) {
        notifySchoolAdmins(
                schoolId,
                notificationType,
                priority,
                title,
                message,
                actionUrl,
                entityType,
                parseEntityId(entityId),
                metadata
        );
    }

    private static Long parseEntityId(String entityId) {
        if (entityId == null || entityId.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(entityId.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
