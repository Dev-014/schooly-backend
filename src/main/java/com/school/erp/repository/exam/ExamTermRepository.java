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

    List<ExamTerm> findBySchoolIdOrderByStartDateDesc(Long schoolId);

    Optional<ExamTerm> findFirstBySchoolIdOrderByStartDateDesc(Long schoolId);

    List<ExamTerm> findBySchoolIdAndAcademicYearIdOrderByStartDateDesc(Long schoolId, Long academicYearId);

    Optional<ExamTerm> findByIdAndSchoolId(Long id, Long schoolId);

    long countBySchoolId(Long schoolId);

    long countBySchoolIdAndStatus(Long schoolId, String status);

    @Query("SELECT COUNT(e) FROM ExamSetup e WHERE e.term.id = :termId")
    long countExamsByTermId(@Param("termId") Long termId);
}
