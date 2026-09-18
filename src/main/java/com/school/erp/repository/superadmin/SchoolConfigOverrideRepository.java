package com.school.erp.repository;

import com.school.erp.entity.SchoolConfigOverride;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SchoolConfigOverrideRepository extends JpaRepository<SchoolConfigOverride, Long> {
    Optional<SchoolConfigOverride> findBySchoolId(Long schoolId);
}
