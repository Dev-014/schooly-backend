package com.school.erp.repository.superadmin;

import com.school.erp.entity.superadmin.EmployeeAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeAuditLogRepository extends JpaRepository<EmployeeAuditLog, Long> {
    List<EmployeeAuditLog> findByEmployeeId(Long employeeId);
}
