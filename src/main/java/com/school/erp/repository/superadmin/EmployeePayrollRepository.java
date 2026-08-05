package com.school.erp.repository.superadmin;

import com.school.erp.entity.superadmin.EmployeePayroll;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeePayrollRepository extends JpaRepository<EmployeePayroll, Long> {
    List<EmployeePayroll> findByEmployeeId(Long employeeId);
    List<EmployeePayroll> findByMonthAndYear(String month, Integer year);
}
