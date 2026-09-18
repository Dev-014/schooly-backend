package com.school.erp.repository;

import com.school.erp.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    List<Attendance> findBySchoolId(Long schoolId);

    List<Attendance> findBySchoolIdAndStudentId(Long schoolId, Long studentId);

    long countBySchoolIdAndAttendanceDateAndStatus(Long schoolId, java.time.LocalDate attendanceDate, String status);

    List<Attendance> findBySchoolIdAndAttendanceDateAndStudentIdIn(Long schoolId, java.time.LocalDate attendanceDate, List<Long> studentIds);

    java.util.Optional<Attendance> findBySchoolIdAndAttendanceDateAndStudentId(Long schoolId, java.time.LocalDate attendanceDate, Long studentId);

    @org.springframework.data.jpa.repository.Query("SELECT a.attendanceDate, " +
            "SUM(CASE WHEN a.status = 'PRESENT' OR a.status = 'LATE' THEN 1 ELSE 0 END), " +
            "COUNT(a.id) " +
            "FROM Attendance a " +
            "WHERE a.school.id = :schoolId AND a.attendanceDate >= :startDate " +
            "GROUP BY a.attendanceDate " +
            "ORDER BY a.attendanceDate ASC")
    List<Object[]> getAttendanceTrendsByDate(@org.springframework.data.repository.query.Param("schoolId") Long schoolId, @org.springframework.data.repository.query.Param("startDate") java.time.LocalDate startDate);

    @org.springframework.data.jpa.repository.Query("SELECT c.name, " +
            "COUNT(DISTINCT s.id), " +
            "SUM(CASE WHEN a.status = 'PRESENT' OR a.status = 'LATE' THEN 1 ELSE 0 END), " +
            "SUM(CASE WHEN a.status = 'LATE' THEN 1 ELSE 0 END), " +
            "COUNT(a.id) " +
            "FROM Attendance a JOIN a.student s JOIN s.schoolClass c " +
            "WHERE a.school.id = :schoolId " +
            "GROUP BY c.id, c.name")
    List<Object[]> getGradeWiseAttendance(@org.springframework.data.repository.query.Param("schoolId") Long schoolId);
}
