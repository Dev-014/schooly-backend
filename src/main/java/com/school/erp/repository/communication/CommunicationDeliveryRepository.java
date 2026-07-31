package com.school.erp.repository.communication;

import com.school.erp.entity.communication.CommunicationDelivery;
import com.school.erp.entity.communication.CommunicationDeliveryStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface CommunicationDeliveryRepository extends JpaRepository<CommunicationDelivery, Long> {

    Page<CommunicationDelivery> findByAnnouncementId(Long announcementId, Pageable pageable);

    @Query("SELECT COUNT(d) FROM CommunicationDelivery d WHERE d.status = :status")
    long countByStatus(@Param("status") CommunicationDeliveryStatus status);
    
    @Query("SELECT COUNT(d) FROM CommunicationDelivery d WHERE d.status = :status AND d.createdAt >= :startDate")
    long countByStatusAndCreatedAtAfter(@Param("status") CommunicationDeliveryStatus status, @Param("startDate") LocalDateTime startDate);
}
