package com.school.erp.entity.notification;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(
        name = "notifications",
        indexes = {
                @Index(name = "idx_notifications_school_user_read", columnList = "school_id, user_id, is_read, created_at"),
                @Index(name = "idx_notifications_school_role", columnList = "school_id, target_role, created_at"),
                @Index(name = "idx_notifications_type", columnList = "school_id, notification_type"),
                @Index(name = "idx_notifications_category", columnList = "school_id, category"),
                @Index(name = "idx_notifications_actor", columnList = "school_id, actor_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "school_id", nullable = false)
    private Long schoolId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "target_role", nullable = false, length = 50)
    private String targetRole; // e.g. ADMIN, TEACHER, PARENT, STAFF, STUDENT

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false, length = 100)
    private NotificationType notificationType;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 50)
    @Builder.Default
    private NotificationCategory category = NotificationCategory.GENERAL;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 20)
    @Builder.Default
    private NotificationPriority priority = NotificationPriority.NORMAL;

    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private Boolean isRead = false;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @Column(name = "actor_id")
    private Long actorId;

    @Column(name = "actor_role", length = 50)
    private String actorRole; // e.g. ADMIN, TEACHER, STAFF, PARENT, SYSTEM

    @Column(name = "actor_name", length = 255)
    private String actorName;

    @Column(name = "action_url", length = 500)
    private String actionUrl;

    @Column(name = "action_type", length = 100)
    @Builder.Default
    private String actionType = "NAVIGATE";

    @Column(name = "entity_type", length = 100)
    private String entityType;

    @Column(name = "entity_id")
    private Long entityId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "jsonb")
    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
