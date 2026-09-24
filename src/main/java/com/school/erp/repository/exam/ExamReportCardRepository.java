package com.school.erp.repository.exam;

import com.school.erp.entity.exam.ExamReportCard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExamReportCardRepository extends JpaRepository<ExamReportCard, Long> {

    Optional<ExamReportCard> findByIdAndSchoolId(Long id, Long schoolId);

    Optional<ExamReportCard> findBySchoolIdAndTermIdAndStudentIdAndGenerationMode(
            Long schoolId, Long termId, Long studentId, String generationMode);

    @Query("SELECT r FROM ExamReportCard r " +
           "WHERE r.school.id = :schoolId " +
           "AND r.term.id = :termId " +
           "AND r.generationMode = :generationMode " +
           "AND r.student.id IN :studentIds")
    List<ExamReportCard> findBySchoolIdAndTermIdAndGenerationModeAndStudentIdIn(
            @Param("schoolId") Long schoolId,
            @Param("termId") Long termId,
            @Param("generationMode") String generationMode,
            @Param("studentIds") java.util.Collection<Long> studentIds);

    @Query("SELECT r FROM ExamReportCard r " +
           "JOIN FETCH r.student s " +
           "JOIN FETCH r.schoolClass sc " +
           "LEFT JOIN FETCH r.section sec " +
           "JOIN FETCH r.term t " +
           "LEFT JOIN FETCH r.examSetup es " +
           "WHERE r.school.id = :schoolId AND r.student.id = :studentId")
    List<ExamReportCard> findBySchoolIdAndStudentId(
            @Param("schoolId") Long schoolId,
            @Param("studentId") Long studentId);

    @Query(value = "SELECT r FROM ExamReportCard r " +
           "JOIN FETCH r.student s " +
           "JOIN FETCH r.schoolClass sc " +
           "LEFT JOIN FETCH r.section sec " +
           "JOIN FETCH r.term t " +
           "LEFT JOIN FETCH r.examSetup es " +
           "WHERE r.school.id = :schoolId " +
           "AND (:termId IS NULL OR r.term.id = :termId) " +
           "AND (:classId IS NULL OR r.schoolClass.id = :classId) " +
           "AND (:sectionId IS NULL OR r.section.id = :sectionId) " +
           "AND (:examSetupId IS NULL OR r.examSetup.id = :examSetupId) " +
           "AND (:status IS NULL OR r.status = CAST(:status AS string)) " +
           "ORDER BY s.rollNumber ASC, s.name ASC",
           countQuery = "SELECT count(r) FROM ExamReportCard r WHERE r.school.id = :schoolId " +
           "AND (:termId IS NULL OR r.term.id = :termId) " +
           "AND (:classId IS NULL OR r.schoolClass.id = :classId) " +
           "AND (:sectionId IS NULL OR r.section.id = :sectionId) " +
           "AND (:examSetupId IS NULL OR r.examSetup.id = :examSetupId) " +
           "AND (:status IS NULL OR r.status = CAST(:status AS string))")
    Page<ExamReportCard> filterReportCards(
            @Param("schoolId") Long schoolId,
            @Param("termId") Long termId,
            @Param("classId") Long classId,
            @Param("sectionId") Long sectionId,
            @Param("examSetupId") Long examSetupId,
            @Param("status") String status,
            Pageable pageable);

    @Query("SELECT COUNT(r) FROM ExamReportCard r WHERE r.school.id = :schoolId " +
           "AND (:termId IS NULL OR r.term.id = :termId) " +
           "AND (:classId IS NULL OR r.schoolClass.id = :classId) " +
           "AND (:sectionId IS NULL OR r.section.id = :sectionId)")
    long countReportCards(
            @Param("schoolId") Long schoolId,
            @Param("termId") Long termId,
            @Param("classId") Long classId,
            @Param("sectionId") Long sectionId);
}
