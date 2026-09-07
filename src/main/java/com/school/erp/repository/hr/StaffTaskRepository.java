package com.school.erp.repository.hr;

import com.school.erp.entity.hr.StaffTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StaffTaskRepository extends JpaRepository<StaffTask, Long> {
    List<StaffTask> findBySchoolId(Long schoolId);
    List<StaffTask> findByStaffId(Long staffId);
}
