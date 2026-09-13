package com.school.erp.repository.exam;

import com.school.erp.entity.exam.ExamSetup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExamSetupRepository extends JpaRepository<ExamSetup, Long> {

    List<ExamSetup> findBySchoolIdOrderByOrderNoAsc(Long schoolId);

    List<ExamSetup> findBySchoolIdAndTermIdOrderByOrderNoAsc(Long schoolId, Long termId);

    Optional<ExamSetup> findByIdAndSchoolId(Long id, Long schoolId);

    long countBySchoolId(Long schoolId);

    long countBySchoolIdAndStatus(Long schoolId, String status);

    long countBySchoolIdAndTermId(Long schoolId, Long termId);

    @Query("SELECT AVG(e.weightagePercent) FROM ExamSetup e WHERE e.school.id = :schoolId AND e.weightageActive = true")
    BigDecimal findAvgWeightageBySchoolId(@Param("schoolId") Long schoolId);

    @Query("SELECT COUNT(s) FROM ExamSchedule s WHERE s.examSetup.id = :examSetupId")
    long countSchedulesByExamSetupId(@Param("examSetupId") Long examSetupId);
}
