package com.school.erp.repository.crm;

import com.school.erp.entity.crm.CrmFollowUp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CrmFollowUpRepository extends JpaRepository<CrmFollowUp, Long> {
    List<CrmFollowUp> findByLeadIdOrderByScheduledDateDesc(Long leadId);
}
