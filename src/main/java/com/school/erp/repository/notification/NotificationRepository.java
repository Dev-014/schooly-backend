package com.school.erp.repository.notification;

import com.school.erp.entity.notification.Notification;
import com.school.erp.entity.notification.NotificationCategory;
import com.school.erp.entity.notification.NotificationPriority;
import com.school.erp.entity.notification.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Optional<Notification> findByIdAndSchoolIdAndUserId(Long id, Long schoolId, Long userId);

    Page<Notification> findBySchoolIdAndUserIdOrderByCreatedAtDesc(Long schoolId, Long userId, Pageable pageable);

    @Query("""
        SELECT n FROM Notification n
        WHERE n.schoolId = :schoolId
          AND n.userId = :userId
          AND (:isRead IS NULL OR n.isRead = :isRead)
          AND (:category IS NULL OR n.category = :category)
          AND (:notificationType IS NULL OR n.notificationType = :notificationType)
          AND (:priority IS NULL OR n.priority = :priority)
        ORDER BY n.createdAt DESC
    """)
    Page<Notification> filterNotifications(
            @Param("schoolId") Long schoolId,
            @Param("userId") Long userId,
            @Param("isRead") Boolean isRead,
            @Param("category") NotificationCategory category,
            @Param("notificationType") NotificationType notificationType,
            @Param("priority") NotificationPriority priority,
            Pageable pageable
    );

    long countBySchoolIdAndUserIdAndIsReadFalse(Long schoolId, Long userId);

    long countBySchoolIdAndUserId(Long schoolId, Long userId);

    @Query("""
        SELECT n.category, COUNT(n) FROM Notification n
        WHERE n.schoolId = :schoolId AND n.userId = :userId AND n.isRead = false
        GROUP BY n.category
    """)
    List<Object[]> countUnreadGroupedByCategory(@Param("schoolId") Long schoolId, @Param("userId") Long userId);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = :readAt WHERE n.schoolId = :schoolId AND n.userId = :userId AND n.isRead = false")
    int markAllAsRead(
            @Param("schoolId") Long schoolId,
            @Param("userId") Long userId,
            @Param("readAt") LocalDateTime readAt
    );

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = :readAt WHERE n.schoolId = :schoolId AND n.userId = :userId AND n.id IN :ids")
    int markBatchAsRead(
            @Param("schoolId") Long schoolId,
            @Param("userId") Long userId,
            @Param("ids") List<Long> ids,
            @Param("readAt") LocalDateTime readAt
    );

    @Modifying
    @Query("DELETE FROM Notification n WHERE n.schoolId = :schoolId AND n.userId = :userId AND n.isRead = true")
    int deleteReadNotifications(
            @Param("schoolId") Long schoolId,
            @Param("userId") Long userId
    );
}
