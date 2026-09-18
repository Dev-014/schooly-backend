package com.school.erp.repository.finance;

import com.school.erp.entity.finance.CollectionPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CollectionPlanRepository extends JpaRepository<CollectionPlan, Long> {
    List<CollectionPlan> findBySchoolId(Long schoolId);
}
