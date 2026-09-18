package com.school.erp.repository;

import com.school.erp.entity.TenantEntitlementOverride;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TenantEntitlementOverrideRepository extends JpaRepository<TenantEntitlementOverride, Long> {
    List<TenantEntitlementOverride> findBySchoolId(Long schoolId);
    List<TenantEntitlementOverride> findBySchoolIdAndModuleCode(Long schoolId, String moduleCode);
}
