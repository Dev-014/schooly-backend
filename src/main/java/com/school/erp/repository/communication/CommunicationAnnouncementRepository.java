package com.school.erp.repository.communication;

import com.school.erp.entity.communication.CommunicationAnnouncement;
import com.school.erp.entity.communication.CommunicationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommunicationAnnouncementRepository extends JpaRepository<CommunicationAnnouncement, Long> {
    Page<CommunicationAnnouncement> findByStatus(CommunicationStatus status, Pageable pageable);
    
    Page<CommunicationAnnouncement> findByStatusIn(List<CommunicationStatus> statuses, Pageable pageable);
}
