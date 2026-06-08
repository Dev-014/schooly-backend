package com.school.erp.repository;

import com.school.erp.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    List<Attendance> findBySchoolId(Long schoolId);

    List<Attendance> findBySchoolIdAndStudentId(Long schoolId, Long studentId);

    long countBySchoolIdAndAttendanceDateAndStatus(Long schoolId, java.time.LocalDate attendanceDate, String status);
}
