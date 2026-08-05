package com.school.erp.repository.superadmin;

import com.school.erp.entity.superadmin.EmployeeLifecycle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeLifecycleRepository extends JpaRepository<EmployeeLifecycle, Long> {
    List<EmployeeLifecycle> findByEmployeeId(Long employeeId);
}
