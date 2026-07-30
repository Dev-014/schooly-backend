package com.school.erp.repository.crm;

import com.school.erp.entity.crm.CrmActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CrmActivityLogRepository extends JpaRepository<CrmActivityLog, Long> {
    List<CrmActivityLog> findByLeadIdOrderByCreatedAtDesc(Long leadId);
}
