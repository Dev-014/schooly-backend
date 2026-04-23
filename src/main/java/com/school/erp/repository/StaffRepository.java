package com.school.erp.repository;

import com.school.erp.entity.Staff;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StaffRepository extends JpaRepository<Staff, Long> {

    List<Staff> findBySchoolId(Long schoolId);

    Optional<Staff> findByIdAndSchoolId(Long id, Long schoolId);

    long countBySchoolId(Long schoolId);
}
