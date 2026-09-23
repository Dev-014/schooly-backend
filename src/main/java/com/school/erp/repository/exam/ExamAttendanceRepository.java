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

    Optional<ExamAttendance> findBySchoolIdAndTermIdAndStudentIdAndExamScheduleId(
            Long schoolId, Long termId, Long studentId, Long examScheduleId);

    @Query(value = "SELECT a FROM ExamAttendance a " +
           "JOIN FETCH a.student s " +
           "JOIN FETCH a.term t " +
           "JOIN FETCH a.schoolClass c " +
           "LEFT JOIN FETCH a.section sec " +
           "LEFT JOIN FETCH a.examSetup setup " +
           "LEFT JOIN FETCH a.examSchedule sch " +
           "LEFT JOIN FETCH sch.subject sub " +
           "WHERE a.school.id = :schoolId " +
           "AND (:termId IS NULL OR a.term.id = :termId) " +
           "AND (:classId IS NULL OR a.schoolClass.id = :classId) " +
           "AND (:sectionId IS NULL OR a.section.id = :sectionId) " +
           "AND (:examSetupId IS NULL OR a.examSetup.id = :examSetupId) " +
           "AND (:subjectId IS NULL OR a.examSchedule.subject.id = :subjectId) " +
           "AND (:status IS NULL OR a.attendanceStatus = CAST(:status AS string)) " +
           "AND (:search IS NULL OR LOWER(s.name) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) " +
           "OR LOWER(s.rollNumber) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) " +
           "OR LOWER(s.admissionNo) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))) " +
           "ORDER BY s.rollNumber ASC, s.name ASC",
           countQuery = "SELECT COUNT(a) FROM ExamAttendance a WHERE a.school.id = :schoolId " +
           "AND (:termId IS NULL OR a.term.id = :termId) " +
           "AND (:classId IS NULL OR a.schoolClass.id = :classId) " +
           "AND (:sectionId IS NULL OR a.section.id = :sectionId) " +
           "AND (:examSetupId IS NULL OR a.examSetup.id = :examSetupId) " +
           "AND (:subjectId IS NULL OR a.examSchedule.subject.id = :subjectId) " +
           "AND (:status IS NULL OR a.attendanceStatus = CAST(:status AS string)) " +
           "AND (:search IS NULL OR LOWER(a.student.name) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) " +
           "OR LOWER(a.student.rollNumber) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) " +
           "OR LOWER(a.student.admissionNo) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')))")
    Page<ExamAttendance> filterAttendance(
            @Param("schoolId") Long schoolId,
            @Param("termId") Long termId,
            @Param("classId") Long classId,
            @Param("sectionId") Long sectionId,
            @Param("examSetupId") Long examSetupId,
            @Param("subjectId") Long subjectId,
            @Param("status") String status,
            @Param("search") String search,
            Pageable pageable);

    @Query("SELECT a FROM ExamAttendance a " +
           "WHERE a.school.id = :schoolId " +
           "AND a.term.id = :termId " +
           "AND a.student.id IN :studentIds")
    List<ExamAttendance> findBySchoolIdAndTermIdAndStudentIdIn(
            @Param("schoolId") Long schoolId,
            @Param("termId") Long termId,
            @Param("studentIds") List<Long> studentIds);

    @Query("SELECT a FROM ExamAttendance a " +
           "WHERE a.school.id = :schoolId " +
           "AND a.term.id = :termId " +
           "AND a.examSchedule.id = :examScheduleId " +
           "AND a.student.id IN :studentIds")
    List<ExamAttendance> findBySchoolIdAndTermIdAndExamScheduleIdAndStudentIdIn(
            @Param("schoolId") Long schoolId,
            @Param("termId") Long termId,
            @Param("examScheduleId") Long examScheduleId,
            @Param("studentIds") List<Long> studentIds);

    @Query("SELECT COUNT(a) FROM ExamAttendance a WHERE a.school.id = :schoolId " +
           "AND (:termId IS NULL OR a.term.id = :termId) " +
           "AND (:examSetupId IS NULL OR a.examSetup.id = :examSetupId) " +
           "AND (:classId IS NULL OR a.schoolClass.id = :classId) " +
           "AND (:sectionId IS NULL OR a.section.id = :sectionId) " +
           "AND (:subjectId IS NULL OR a.examSchedule.subject.id = :subjectId) " +
           "AND a.attendanceStatus = 'PRESENT'")
    long countPresent(
            @Param("schoolId") Long schoolId,
            @Param("termId") Long termId,
            @Param("examSetupId") Long examSetupId,
            @Param("classId") Long classId,
            @Param("sectionId") Long sectionId,
            @Param("subjectId") Long subjectId);

    @Query("SELECT COUNT(a) FROM ExamAttendance a WHERE a.school.id = :schoolId " +
           "AND (:termId IS NULL OR a.term.id = :termId) " +
           "AND (:examSetupId IS NULL OR a.examSetup.id = :examSetupId) " +
           "AND (:classId IS NULL OR a.schoolClass.id = :classId) " +
           "AND (:sectionId IS NULL OR a.section.id = :sectionId) " +
           "AND (:subjectId IS NULL OR a.examSchedule.subject.id = :subjectId) " +
           "AND a.attendanceStatus = 'ABSENT'")
    long countAbsent(
            @Param("schoolId") Long schoolId,
            @Param("termId") Long termId,
            @Param("examSetupId") Long examSetupId,
            @Param("classId") Long classId,
            @Param("sectionId") Long sectionId,
            @Param("subjectId") Long subjectId);

    @Query("SELECT COUNT(a) FROM ExamAttendance a WHERE a.school.id = :schoolId " +
           "AND (:termId IS NULL OR a.term.id = :termId) " +
           "AND (:examSetupId IS NULL OR a.examSetup.id = :examSetupId) " +
           "AND (:classId IS NULL OR a.schoolClass.id = :classId) " +
           "AND (:sectionId IS NULL OR a.section.id = :sectionId) " +
           "AND (:subjectId IS NULL OR a.examSchedule.subject.id = :subjectId) " +
           "AND a.attendanceStatus = 'LEAVE'")
    long countLeave(
            @Param("schoolId") Long schoolId,
            @Param("termId") Long termId,
            @Param("examSetupId") Long examSetupId,
            @Param("classId") Long classId,
            @Param("sectionId") Long sectionId,
            @Param("subjectId") Long subjectId);

    @Query("SELECT COUNT(a) FROM ExamAttendance a WHERE a.school.id = :schoolId " +
           "AND (:termId IS NULL OR a.term.id = :termId) " +
           "AND (:examSetupId IS NULL OR a.examSetup.id = :examSetupId) " +
           "AND (:classId IS NULL OR a.schoolClass.id = :classId) " +
           "AND (:sectionId IS NULL OR a.section.id = :sectionId) " +
           "AND (:subjectId IS NULL OR a.examSchedule.subject.id = :subjectId)")
    long countTotal(
            @Param("schoolId") Long schoolId,
            @Param("termId") Long termId,
            @Param("examSetupId") Long examSetupId,
            @Param("classId") Long classId,
            @Param("sectionId") Long sectionId,
            @Param("subjectId") Long subjectId);

    List<ExamAttendance> findBySchoolIdAndTermIdAndSchoolClassId(Long schoolId, Long termId, Long classId);
}
