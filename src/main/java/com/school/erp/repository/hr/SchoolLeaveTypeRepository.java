package com.school.erp.repository.hr;

import com.school.erp.entity.hr.SchoolLeaveType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SchoolLeaveTypeRepository extends JpaRepository<SchoolLeaveType, Long> {
    List<SchoolLeaveType> findBySchoolId(Long schoolId);
}
