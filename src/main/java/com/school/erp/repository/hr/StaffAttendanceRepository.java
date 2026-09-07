package com.school.erp.repository.hr;

import com.school.erp.entity.hr.StaffAttendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StaffAttendanceRepository extends JpaRepository<StaffAttendance, Long> {
    List<StaffAttendance> findBySchoolId(Long schoolId);
    List<StaffAttendance> findByStaffId(Long staffId);
    java.util.Optional<StaffAttendance> findByStaffIdAndAttendanceDate(Long staffId, java.time.LocalDate date);
    List<StaffAttendance> findBySchoolIdAndAttendanceDate(Long schoolId, java.time.LocalDate date);
}
