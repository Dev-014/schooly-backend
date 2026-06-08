package com.school.erp.repository;

import com.school.erp.entity.SchoolModuleAccess;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SchoolModuleAccessRepository extends JpaRepository<SchoolModuleAccess, Long> {
    Optional<SchoolModuleAccess> findBySchoolIdAndModuleId(Long schoolId, Long moduleId);

    List<SchoolModuleAccess> findBySchoolId(Long schoolId);

    List<SchoolModuleAccess> findByModuleId(Long moduleId);
}
