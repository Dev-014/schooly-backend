package com.school.erp.repository.hr;

import com.school.erp.entity.hr.StaffLeave;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StaffLeaveRepository extends JpaRepository<StaffLeave, Long> {
    List<StaffLeave> findBySchoolId(Long schoolId);
    List<StaffLeave> findByStaffId(Long staffId);
}
