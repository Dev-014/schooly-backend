package com.school.erp.repository.auth;

import com.school.erp.entity.auth.RolePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RolePermissionRepository extends JpaRepository<RolePermission, Long> {
    List<RolePermission> findBySchoolIdAndRoleId(Long schoolId, String roleId);
}
