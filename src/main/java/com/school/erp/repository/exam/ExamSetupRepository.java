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

    @Query("SELECT e FROM ExamSetup e " +
           "JOIN FETCH e.term t " +
           "WHERE e.school.id = :schoolId " +
           "ORDER BY e.orderNo ASC")
    List<ExamSetup> findBySchoolIdOrderByOrderNoAsc(@Param("schoolId") Long schoolId);

    @Query("SELECT e FROM ExamSetup e " +
           "JOIN FETCH e.term t " +
           "WHERE e.school.id = :schoolId AND e.term.id = :termId " +
           "ORDER BY e.orderNo ASC")
    List<ExamSetup> findBySchoolIdAndTermIdOrderByOrderNoAsc(
            @Param("schoolId") Long schoolId,
            @Param("termId") Long termId);

    Optional<ExamSetup> findByIdAndSchoolId(Long id, Long schoolId);

    long countBySchoolId(Long schoolId);

    long countBySchoolIdAndStatus(Long schoolId, String status);

    long countBySchoolIdAndTermId(Long schoolId, Long termId);

    @Query("SELECT AVG(e.weightagePercent) FROM ExamSetup e WHERE e.school.id = :schoolId AND e.weightageActive = true")
    BigDecimal findAvgWeightageBySchoolId(@Param("schoolId") Long schoolId);

    @Query("SELECT COUNT(s) FROM ExamSchedule s WHERE s.examSetup.id = :examSetupId")
    long countSchedulesByExamSetupId(@Param("examSetupId") Long examSetupId);

    @Query("SELECT s.examSetup.id, COUNT(s) FROM ExamSchedule s WHERE s.examSetup.id IN :setupIds GROUP BY s.examSetup.id")
    List<Object[]> countSchedulesByExamSetupIdIn(@Param("setupIds") java.util.Collection<Long> setupIds);

    @Query("SELECT " +
           "COUNT(e), " +
           "COALESCE(SUM(CASE WHEN e.status = 'ACTIVE' THEN 1L ELSE 0L END), 0L), " +
           "COALESCE(SUM(CASE WHEN e.status = 'PENDING' THEN 1L ELSE 0L END), 0L), " +
           "AVG(CASE WHEN e.weightageActive = true THEN e.weightagePercent ELSE NULL END) " +
           "FROM ExamSetup e WHERE e.school.id = :schoolId")
    List<Object[]> getSetupMetrics(@Param("schoolId") Long schoolId);
}
