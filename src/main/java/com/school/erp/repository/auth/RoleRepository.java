package com.school.erp.repository.auth;

import com.school.erp.entity.auth.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, String> {
    @Query("SELECT r FROM Role r WHERE r.schoolId = :schoolId OR (r.isSystemRole = true AND r.schoolId IS NULL)")
    List<Role> findBySchoolIdOrSystemRoles(@Param("schoolId") Long schoolId);
    
    Optional<Role> findByIdAndSchoolId(String id, Long schoolId);
}
