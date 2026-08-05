package com.school.erp.repository.superadmin;

import com.school.erp.entity.superadmin.EmployeePerformance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeePerformanceRepository extends JpaRepository<EmployeePerformance, Long> {
    List<EmployeePerformance> findByEmployeeId(Long employeeId);
}
