package com.school.erp.repository.notification;

import com.school.erp.entity.notification.NotificationDeviceToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationDeviceTokenRepository extends JpaRepository<NotificationDeviceToken, Long> {

    Optional<NotificationDeviceToken> findByUserIdAndToken(Long userId, String token);

    List<NotificationDeviceToken> findBySchoolIdAndUserIdInAndActiveTrue(Long schoolId, Collection<Long> userIds);

    List<NotificationDeviceToken> findByToken(String token);

    @Modifying
    @Query("UPDATE NotificationDeviceToken t SET t.active = false WHERE t.userId = :userId AND t.token = :token")
    int deactivateToken(@Param("userId") Long userId, @Param("token") String token);
}
