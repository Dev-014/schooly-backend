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

    @Query("SELECT r FROM ExamTeacherRemark r " +
           "WHERE r.school.id = :schoolId " +
           "AND r.term.id = :termId " +
           "AND r.student.id IN :studentIds")
    List<ExamTeacherRemark> findBySchoolIdAndTermIdAndStudentIdIn(
            @Param("schoolId") Long schoolId,
            @Param("termId") Long termId,
            @Param("studentIds") java.util.Collection<Long> studentIds);

    @Query(value = "SELECT r FROM ExamTeacherRemark r " +
           "JOIN FETCH r.student s " +
           "JOIN FETCH r.schoolClass sc " +
           "LEFT JOIN FETCH r.section sec " +
           "JOIN FETCH r.term t " +
           "WHERE r.school.id = :schoolId " +
           "AND (:termId IS NULL OR r.term.id = :termId) " +
           "AND (:classId IS NULL OR r.schoolClass.id = :classId) " +
           "AND (:sectionId IS NULL OR r.section.id = :sectionId) " +
           "ORDER BY s.rollNumber ASC, s.name ASC",
           countQuery = "SELECT count(r) FROM ExamTeacherRemark r WHERE r.school.id = :schoolId " +
           "AND (:termId IS NULL OR r.term.id = :termId) " +
           "AND (:classId IS NULL OR r.schoolClass.id = :classId) " +
           "AND (:sectionId IS NULL OR r.section.id = :sectionId)")
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
