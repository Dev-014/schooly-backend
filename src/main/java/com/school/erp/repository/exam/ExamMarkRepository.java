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

    List<ExamMark> findBySchoolIdAndExamSetupIdAndStudentId(Long schoolId, Long examSetupId, Long studentId);

    List<ExamMark> findBySchoolIdAndExamSetupTermIdAndStudentId(Long schoolId, Long termId, Long studentId);

    @Query(value = "SELECT m FROM ExamMark m " +
           "JOIN FETCH m.student s " +
           "JOIN FETCH m.schoolClass c " +
           "LEFT JOIN FETCH m.section sec " +
           "JOIN FETCH m.subject sub " +
           "JOIN FETCH m.examSetup setup " +
           "LEFT JOIN FETCH m.examSubjectConfig esc " +
           "WHERE m.school.id = :schoolId " +
           "AND (:examSetupId IS NULL OR m.examSetup.id = :examSetupId) " +
           "AND (:classId IS NULL OR m.schoolClass.id = :classId) " +
           "AND (:sectionId IS NULL OR m.section.id = :sectionId) " +
           "AND (:subjectId IS NULL OR m.subject.id = :subjectId) " +
           "ORDER BY s.rollNumber ASC, s.name ASC",
           countQuery = "SELECT COUNT(m) FROM ExamMark m WHERE m.school.id = :schoolId " +
           "AND (:examSetupId IS NULL OR m.examSetup.id = :examSetupId) " +
           "AND (:classId IS NULL OR m.schoolClass.id = :classId) " +
           "AND (:sectionId IS NULL OR m.section.id = :sectionId) " +
           "AND (:subjectId IS NULL OR m.subject.id = :subjectId)")
    Page<ExamMark> filterMarks(
            @Param("schoolId") Long schoolId,
            @Param("examSetupId") Long examSetupId,
            @Param("classId") Long classId,
            @Param("sectionId") Long sectionId,
            @Param("subjectId") Long subjectId,
            Pageable pageable);

    List<ExamMark> findBySchoolIdAndExamSetupIdAndSubjectId(Long schoolId, Long examSetupId, Long subjectId);

    @Query("SELECT m FROM ExamMark m " +
           "WHERE m.school.id = :schoolId " +
           "AND m.examSetup.id = :examSetupId " +
           "AND m.subject.id = :subjectId " +
           "AND m.schoolClass.id = :classId")
    List<ExamMark> findBySchoolIdAndExamSetupIdAndSubjectIdAndClassId(
            @Param("schoolId") Long schoolId,
            @Param("examSetupId") Long examSetupId,
            @Param("subjectId") Long subjectId,
            @Param("classId") Long classId);

    @Query("SELECT m FROM ExamMark m " +
           "JOIN FETCH m.subject sub " +
           "LEFT JOIN FETCH m.examSubjectConfig esc " +
           "WHERE m.school.id = :schoolId " +
           "AND m.examSetup.id = :examSetupId " +
           "AND m.student.id IN :studentIds")
    List<ExamMark> findBySchoolIdAndExamSetupIdAndStudentIdIn(
            @Param("schoolId") Long schoolId,
            @Param("examSetupId") Long examSetupId,
            @Param("studentIds") List<Long> studentIds);

    @Query("SELECT m FROM ExamMark m " +
           "JOIN FETCH m.subject sub " +
           "LEFT JOIN FETCH m.examSubjectConfig esc " +
           "WHERE m.school.id = :schoolId " +
           "AND m.examSetup.term.id = :termId " +
           "AND m.student.id IN :studentIds")
    List<ExamMark> findBySchoolIdAndExamSetupTermIdAndStudentIdIn(
            @Param("schoolId") Long schoolId,
            @Param("termId") Long termId,
            @Param("studentIds") List<Long> studentIds);

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
