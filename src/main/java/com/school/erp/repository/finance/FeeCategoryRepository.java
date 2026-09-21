package com.school.erp.repository.finance;

import com.school.erp.entity.finance.FeeCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeeCategoryRepository extends JpaRepository<FeeCategory, Long> {
    List<FeeCategory> findBySchoolId(Long schoolId);
}
