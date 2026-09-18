package com.school.erp.repository;

import com.school.erp.entity.DataImportJob;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DataImportJobRepository extends JpaRepository<DataImportJob, Long> {
    List<DataImportJob> findBySchoolIdOrderByCreatedAtDesc(Long schoolId);
    List<DataImportJob> findBySchoolIdAndCategoryOrderByCreatedAtDesc(Long schoolId, String category);
}
