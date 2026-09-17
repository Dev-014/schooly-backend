package com.school.erp.repository.exam;

import com.school.erp.entity.exam.ExamAttendance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExamAttendanceRepository extends JpaRepository<ExamAttendance, Long> {

    Optional<ExamAttendance> findByIdAndSchoolId(Long id, Long schoolId);

    Optional<ExamAttendance> findBySchoolIdAndTermIdAndStudentId(Long schoolId, Long termId, Long studentId);

    @Query("SELECT a FROM ExamAttendance a WHERE a.school.id = :schoolId " +
           "AND (:termId IS NULL OR a.term.id = :termId) " +
           "AND (:classId IS NULL OR a.schoolClass.id = :classId) " +
           "AND (:sectionId IS NULL OR a.section.id = :sectionId) " +
           "AND (:examSetupId IS NULL OR a.examSetup.id = :examSetupId) " +
           "AND (:status IS NULL OR a.attendanceStatus = CAST(:status AS string)) " +
           "AND (:search IS NULL OR LOWER(a.student.name) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) " +
           "OR LOWER(a.student.rollNumber) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) " +
           "OR LOWER(a.student.admissionNo) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))) " +
           "ORDER BY a.student.rollNumber ASC, a.student.name ASC")
    Page<ExamAttendance> filterAttendance(
            @Param("schoolId") Long schoolId,
            @Param("termId") Long termId,
            @Param("classId") Long classId,
            @Param("sectionId") Long sectionId,
            @Param("examSetupId") Long examSetupId,
            @Param("status") String status,
            @Param("search") String search,
            Pageable pageable);

    @Query("SELECT COUNT(a) FROM ExamAttendance a WHERE a.school.id = :schoolId " +
           "AND (:termId IS NULL OR a.term.id = :termId) " +
           "AND (:classId IS NULL OR a.schoolClass.id = :classId) " +
           "AND (:sectionId IS NULL OR a.section.id = :sectionId) " +
           "AND a.attendanceStatus = 'PRESENT'")
    long countPresent(
            @Param("schoolId") Long schoolId,
            @Param("termId") Long termId,
            @Param("classId") Long classId,
            @Param("sectionId") Long sectionId);

    @Query("SELECT COUNT(a) FROM ExamAttendance a WHERE a.school.id = :schoolId " +
           "AND (:termId IS NULL OR a.term.id = :termId) " +
           "AND (:classId IS NULL OR a.schoolClass.id = :classId) " +
           "AND (:sectionId IS NULL OR a.section.id = :sectionId) " +
           "AND a.attendanceStatus = 'ABSENT'")
    long countAbsent(
            @Param("schoolId") Long schoolId,
            @Param("termId") Long termId,
            @Param("classId") Long classId,
            @Param("sectionId") Long sectionId);

    @Query("SELECT COUNT(a) FROM ExamAttendance a WHERE a.school.id = :schoolId " +
           "AND (:termId IS NULL OR a.term.id = :termId) " +
           "AND (:classId IS NULL OR a.schoolClass.id = :classId) " +
           "AND (:sectionId IS NULL OR a.section.id = :sectionId) " +
           "AND a.attendanceStatus = 'LEAVE'")
    long countLeave(
            @Param("schoolId") Long schoolId,
            @Param("termId") Long termId,
            @Param("classId") Long classId,
            @Param("sectionId") Long sectionId);

    @Query("SELECT COUNT(a) FROM ExamAttendance a WHERE a.school.id = :schoolId " +
           "AND (:termId IS NULL OR a.term.id = :termId) " +
           "AND (:classId IS NULL OR a.schoolClass.id = :classId) " +
           "AND (:sectionId IS NULL OR a.section.id = :sectionId)")
    long countTotal(
            @Param("schoolId") Long schoolId,
            @Param("termId") Long termId,
            @Param("classId") Long classId,
            @Param("sectionId") Long sectionId);

    List<ExamAttendance> findBySchoolIdAndTermIdAndSchoolClassId(Long schoolId, Long termId, Long classId);
}
