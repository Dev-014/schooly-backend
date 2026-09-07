package com.school.erp.repository.hr;

import com.school.erp.entity.hr.StaffPayroll;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StaffPayrollRepository extends JpaRepository<StaffPayroll, Long> {
    List<StaffPayroll> findBySchoolId(Long schoolId);
    List<StaffPayroll> findByStaffId(Long staffId);
    java.util.Optional<StaffPayroll> findByStaffIdAndPayrollMonthAndPayrollYear(Long staffId, String month, Integer year);
}
