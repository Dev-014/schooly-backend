package com.school.erp.repository.hr;

import com.school.erp.entity.hr.StaffLeave;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StaffLeaveRepository extends JpaRepository<StaffLeave, Long> {
    @EntityGraph(attributePaths = {"staff", "school"})
    List<StaffLeave> findBySchoolId(Long schoolId);

    @EntityGraph(attributePaths = {"staff", "school"})
    List<StaffLeave> findByStaffId(Long staffId);
}

