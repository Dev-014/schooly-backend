package com.school.erp.repository.hr;

import com.school.erp.entity.hr.StaffAdvance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StaffAdvanceRepository extends JpaRepository<StaffAdvance, Long> {
    List<StaffAdvance> findBySchoolId(Long schoolId);
    List<StaffAdvance> findByStaffId(Long staffId);
}
