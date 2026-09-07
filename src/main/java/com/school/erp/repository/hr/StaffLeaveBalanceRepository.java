package com.school.erp.repository.hr;

import com.school.erp.entity.hr.StaffLeaveBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StaffLeaveBalanceRepository extends JpaRepository<StaffLeaveBalance, Long> {
    List<StaffLeaveBalance> findBySchoolId(Long schoolId);
    List<StaffLeaveBalance> findByStaffId(Long staffId);
    java.util.Optional<StaffLeaveBalance> findByStaffIdAndLeaveTypeId(Long staffId, Long leaveTypeId);
}
