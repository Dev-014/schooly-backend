package com.school.erp.repository.superadmin;

import com.school.erp.entity.superadmin.EmployeeTimeline;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeTimelineRepository extends JpaRepository<EmployeeTimeline, Long> {
    List<EmployeeTimeline> findByEmployeeIdOrderByDateDesc(Long employeeId);
}
