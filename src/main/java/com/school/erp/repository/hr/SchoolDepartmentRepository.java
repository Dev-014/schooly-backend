package com.school.erp.repository.hr;

import com.school.erp.entity.hr.SchoolDepartment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SchoolDepartmentRepository extends JpaRepository<SchoolDepartment, Long> {
    List<SchoolDepartment> findBySchoolId(Long schoolId);
}
