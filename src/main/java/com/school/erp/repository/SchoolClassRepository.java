package com.school.erp.repository;

import com.school.erp.entity.SchoolClass;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SchoolClassRepository extends JpaRepository<SchoolClass, Long> {

    List<SchoolClass> findBySchoolId(Long schoolId);

    Optional<SchoolClass> findByIdAndSchoolId(Long id, Long schoolId);
}
