package com.school.erp.repository.auth;

import com.school.erp.entity.auth.RolePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RolePermissionRepository extends JpaRepository<RolePermission, Long> {
    @Query("SELECT rp FROM RolePermission rp WHERE rp.role.id = :roleId AND (rp.schoolId = :schoolId OR rp.schoolId IS NULL)")
    List<RolePermission> findBySchoolIdAndRoleId(@Param("schoolId") Long schoolId, @Param("roleId") String roleId);
}
