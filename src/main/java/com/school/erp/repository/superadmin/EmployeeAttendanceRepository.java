package com.school.erp.repository.superadmin;

import com.school.erp.entity.superadmin.EmployeeAttendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeAttendanceRepository extends JpaRepository<EmployeeAttendance, Long> {
    List<EmployeeAttendance> findByEmployeeId(Long employeeId);
    Optional<EmployeeAttendance> findByEmployeeIdAndDate(Long employeeId, LocalDate date);
    List<EmployeeAttendance> findByDate(LocalDate date);
}
