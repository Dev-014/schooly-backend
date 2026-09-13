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

    List<ExamReportCard> findBySchoolIdAndStudentId(Long schoolId, Long studentId);

    @Query("SELECT r FROM ExamReportCard r WHERE r.school.id = :schoolId " +
           "AND (:termId IS NULL OR r.term.id = :termId) " +
           "AND (:classId IS NULL OR r.schoolClass.id = :classId) " +
           "AND (:sectionId IS NULL OR r.section.id = :sectionId) " +
           "AND (:examSetupId IS NULL OR r.examSetup.id = :examSetupId) " +
           "AND (:status IS NULL OR r.status = CAST(:status AS string)) " +
           "ORDER BY r.student.rollNumber ASC, r.student.name ASC")
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
