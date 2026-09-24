package com.school.erp.repository.exam;

import com.school.erp.entity.exam.ExamTerm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExamTermRepository extends JpaRepository<ExamTerm, Long> {

    @Query("SELECT t FROM ExamTerm t " +
           "LEFT JOIN FETCH t.academicYear ay " +
           "WHERE t.school.id = :schoolId " +
           "ORDER BY t.startDate DESC")
    List<ExamTerm> findBySchoolIdOrderByStartDateDesc(@Param("schoolId") Long schoolId);

    Optional<ExamTerm> findFirstBySchoolIdOrderByStartDateDesc(Long schoolId);

    @Query("SELECT t FROM ExamTerm t " +
           "LEFT JOIN FETCH t.academicYear ay " +
           "WHERE t.school.id = :schoolId AND t.academicYear.id = :academicYearId " +
           "ORDER BY t.startDate DESC")
    List<ExamTerm> findBySchoolIdAndAcademicYearIdOrderByStartDateDesc(
            @Param("schoolId") Long schoolId,
            @Param("academicYearId") Long academicYearId);

    Optional<ExamTerm> findByIdAndSchoolId(Long id, Long schoolId);

    long countBySchoolId(Long schoolId);

    long countBySchoolIdAndStatus(Long schoolId, String status);

    @Query("SELECT COUNT(e) FROM ExamSetup e WHERE e.term.id = :termId")
    long countExamsByTermId(@Param("termId") Long termId);

    @Query("SELECT e.term.id, COUNT(e) FROM ExamSetup e WHERE e.term.id IN :termIds GROUP BY e.term.id")
    List<Object[]> countExamsByTermIdIn(@Param("termIds") java.util.Collection<Long> termIds);

    @Query("SELECT " +
           "COUNT(t), " +
           "COALESCE(SUM(CASE WHEN t.status = 'ACTIVE' THEN 1L ELSE 0L END), 0L), " +
           "COALESCE(SUM(CASE WHEN t.status = 'SCHEDULED' THEN 1L ELSE 0L END), 0L), " +
           "COALESCE(SUM(CASE WHEN t.status = 'COMPLETED' THEN 1L ELSE 0L END), 0L) " +
           "FROM ExamTerm t WHERE t.school.id = :schoolId")
    List<Object[]> getTermStatusCounts(@Param("schoolId") Long schoolId);
}
