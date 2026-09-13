package com.school.erp.repository.exam;

import com.school.erp.entity.exam.ExamTeacherRemark;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExamTeacherRemarkRepository extends JpaRepository<ExamTeacherRemark, Long> {

    Optional<ExamTeacherRemark> findByIdAndSchoolId(Long id, Long schoolId);

    Optional<ExamTeacherRemark> findBySchoolIdAndTermIdAndStudentId(Long schoolId, Long termId, Long studentId);

    @Query("SELECT r FROM ExamTeacherRemark r WHERE r.school.id = :schoolId " +
           "AND (:termId IS NULL OR r.term.id = :termId) " +
           "AND (:classId IS NULL OR r.schoolClass.id = :classId) " +
           "AND (:sectionId IS NULL OR r.section.id = :sectionId) " +
           "ORDER BY r.student.rollNumber ASC, r.student.name ASC")
    Page<ExamTeacherRemark> filterRemarks(
            @Param("schoolId") Long schoolId,
            @Param("termId") Long termId,
            @Param("classId") Long classId,
            @Param("sectionId") Long sectionId,
            Pageable pageable);

    List<ExamTeacherRemark> findBySchoolIdAndTermIdAndSchoolClassId(Long schoolId, Long termId, Long classId);

    long countBySchoolIdAndTermId(Long schoolId, Long termId);

    long countBySchoolIdAndTermIdAndStatus(Long schoolId, Long termId, String status);
}
