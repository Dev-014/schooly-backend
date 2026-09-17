package com.school.erp.repository.exam;

import com.school.erp.entity.exam.ExamMark;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExamMarkRepository extends JpaRepository<ExamMark, Long> {

    Optional<ExamMark> findByIdAndSchoolId(Long id, Long schoolId);

    Optional<ExamMark> findBySchoolIdAndExamSetupIdAndSubjectIdAndStudentId(
            Long schoolId, Long examSetupId, Long subjectId, Long studentId);

    @Query("SELECT m FROM ExamMark m WHERE m.school.id = :schoolId " +
           "AND (:examSetupId IS NULL OR m.examSetup.id = :examSetupId) " +
           "AND (:classId IS NULL OR m.schoolClass.id = :classId) " +
           "AND (:sectionId IS NULL OR m.section.id = :sectionId) " +
           "AND (:subjectId IS NULL OR m.subject.id = :subjectId) " +
           "ORDER BY m.student.rollNumber ASC, m.student.name ASC")
    Page<ExamMark> filterMarks(
            @Param("schoolId") Long schoolId,
            @Param("examSetupId") Long examSetupId,
            @Param("classId") Long classId,
            @Param("sectionId") Long sectionId,
            @Param("subjectId") Long subjectId,
            Pageable pageable);

    List<ExamMark> findBySchoolIdAndExamSetupIdAndSubjectId(Long schoolId, Long examSetupId, Long subjectId);

    @Query("SELECT AVG(m.marksObtained) FROM ExamMark m WHERE m.school.id = :schoolId " +
           "AND m.examSetup.id = :examSetupId AND m.subject.id = :subjectId AND m.marksObtained IS NOT NULL")
    BigDecimal findAvgMarks(
            @Param("schoolId") Long schoolId,
            @Param("examSetupId") Long examSetupId,
            @Param("subjectId") Long subjectId);

    @Query("SELECT MAX(m.marksObtained) FROM ExamMark m WHERE m.school.id = :schoolId " +
           "AND m.examSetup.id = :examSetupId AND m.subject.id = :subjectId AND m.marksObtained IS NOT NULL")
    BigDecimal findMaxMarks(
            @Param("schoolId") Long schoolId,
            @Param("examSetupId") Long examSetupId,
            @Param("subjectId") Long subjectId);

    @Query("SELECT COUNT(m) FROM ExamMark m WHERE m.school.id = :schoolId " +
           "AND m.examSetup.id = :examSetupId AND m.subject.id = :subjectId AND m.marksObtained IS NOT NULL AND m.marksObtained < 40.0")
    long countBelowForty(
            @Param("schoolId") Long schoolId,
            @Param("examSetupId") Long examSetupId,
            @Param("subjectId") Long subjectId);

    @Query("SELECT COUNT(m) FROM ExamMark m WHERE m.school.id = :schoolId " +
           "AND m.examSetup.id = :examSetupId AND m.subject.id = :subjectId AND m.marksObtained IS NOT NULL")
    long countEnteredMarks(
            @Param("schoolId") Long schoolId,
            @Param("examSetupId") Long examSetupId,
            @Param("subjectId") Long subjectId);
}
