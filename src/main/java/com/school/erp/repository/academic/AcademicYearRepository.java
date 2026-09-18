package com.school.erp.repository.academic;

import com.school.erp.entity.academic.AcademicYear;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AcademicYearRepository extends JpaRepository<AcademicYear, Long> {
    List<AcademicYear> findBySchoolId(Long schoolId);
    Optional<AcademicYear> findByIdAndSchoolId(Long id, Long schoolId);
}
