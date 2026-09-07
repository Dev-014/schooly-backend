package com.school.erp.repository.hr;

import com.school.erp.entity.hr.StaffPayrollDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StaffPayrollDetailsRepository extends JpaRepository<StaffPayrollDetails, Long> {
    StaffPayrollDetails findByStaffId(Long staffId);
}
