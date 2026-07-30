package com.school.erp.repository.crm;

import com.school.erp.entity.crm.CrmDemo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CrmDemoRepository extends JpaRepository<CrmDemo, Long> {
    List<CrmDemo> findByLeadIdOrderByDemoDateDesc(Long leadId);
}
