package com.school.erp.repository;

import com.school.erp.entity.FeeCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeeCategoryRepository extends JpaRepository<FeeCategory, Long> {
    List<FeeCategory> findBySchoolId(Long schoolId);
}
