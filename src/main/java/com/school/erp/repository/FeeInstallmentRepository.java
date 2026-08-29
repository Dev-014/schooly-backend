package com.school.erp.repository;

import com.school.erp.entity.FeeInstallment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeeInstallmentRepository extends JpaRepository<FeeInstallment, Long> {
    List<FeeInstallment> findBySchoolId(Long schoolId);
    List<FeeInstallment> findBySchoolIdAndAcademicYearId(Long schoolId, Long academicYearId);
}
