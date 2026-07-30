package com.school.erp.repository.crm;

import com.school.erp.entity.crm.CrmLead;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CrmLeadRepository extends JpaRepository<CrmLead, Long> {
}
