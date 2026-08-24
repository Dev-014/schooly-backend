package com.school.erp.repository;

import com.school.erp.entity.FeeStructure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeeStructureRepository extends JpaRepository<FeeStructure, Long> {
    List<FeeStructure> findBySchoolId(Long schoolId);
    List<FeeStructure> findBySchoolIdAndAcademicYearId(Long schoolId, Long academicYearId);
}
