package com.school.erp.repository.superadmin;

import com.school.erp.entity.superadmin.EmployeeLeave;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeLeaveRepository extends JpaRepository<EmployeeLeave, Long> {
    List<EmployeeLeave> findByEmployeeId(Long employeeId);
    List<EmployeeLeave> findByStatus(String status);
}
