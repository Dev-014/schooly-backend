package com.school.erp.repository.crm;

import com.school.erp.entity.crm.CrmQuotation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CrmQuotationRepository extends JpaRepository<CrmQuotation, Long> {
    List<CrmQuotation> findByLeadIdOrderByCreatedAtDesc(Long leadId);
}
