package com.school.erp.repository.auth;

import com.school.erp.entity.auth.PermissionDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PermissionDefinitionRepository extends JpaRepository<PermissionDefinition, String> {
    Optional<PermissionDefinition> findByPermissionKey(String permissionKey);
}
