package com.school.erp.repository.superadmin;

import com.school.erp.entity.superadmin.PlatformModule;
import com.school.erp.entity.superadmin.School;
import com.school.erp.entity.superadmin.SchoolModuleAccess;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SchoolModuleAccessRepository extends JpaRepository<SchoolModuleAccess, Long> {
    Optional<SchoolModuleAccess> findBySchoolAndModule(School school, PlatformModule module);

    Optional<SchoolModuleAccess> findBySchoolIdAndModuleId(Long schoolId, Long moduleId);

    List<SchoolModuleAccess> findBySchoolId(Long schoolId);

    List<SchoolModuleAccess> findByModuleId(Long moduleId);
}
