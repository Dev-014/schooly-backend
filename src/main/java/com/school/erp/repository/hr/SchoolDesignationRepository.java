package com.school.erp.repository.hr;

import com.school.erp.entity.hr.SchoolDesignation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SchoolDesignationRepository extends JpaRepository<SchoolDesignation, Long> {
    List<SchoolDesignation> findBySchoolId(Long schoolId);
}
