package com.school.erp.repository;

import com.school.erp.entity.SubjectAttendance;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SubjectAttendanceRepository extends JpaRepository<SubjectAttendance, Long> {

    @EntityGraph(attributePaths = {"subject", "schoolClass", "section", "student"})
    List<SubjectAttendance> findBySchoolIdAndStudentId(Long schoolId, Long studentId);

    @EntityGraph(attributePaths = {"subject", "schoolClass", "section", "student"})
    List<SubjectAttendance> findBySchoolIdAndSchoolClassIdAndSubjectIdAndAttendanceDate(
            Long schoolId, Long classId, Long subjectId, LocalDate attendanceDate);

    @EntityGraph(attributePaths = {"subject", "schoolClass", "section", "student"})
    List<SubjectAttendance> findBySchoolIdAndSchoolClassIdAndSectionIdAndSubjectIdAndAttendanceDate(
            Long schoolId, Long classId, Long sectionId, Long subjectId, LocalDate attendanceDate);


    Optional<SubjectAttendance> findBySchoolIdAndStudentIdAndSubjectIdAndAttendanceDateAndTimetableEntryId(
            Long schoolId, Long studentId, Long subjectId, LocalDate attendanceDate, Long timetableEntryId);

    @Query("SELECT sa.subject.id, sa.subject.name, sa.subject.code, " +
           "COUNT(sa.id), " +
           "SUM(CASE WHEN sa.status = 'PRESENT' OR sa.status = 'LATE' THEN 1 ELSE 0 END) " +
           "FROM SubjectAttendance sa " +
           "WHERE sa.school.id = :schoolId AND sa.student.id = :studentId " +
           "GROUP BY sa.subject.id, sa.subject.name, sa.subject.code")
    List<Object[]> getSubjectWiseAttendanceSummary(
            @Param("schoolId") Long schoolId,
            @Param("studentId") Long studentId);
}
