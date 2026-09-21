package com.school.erp.service.auth;

import com.school.erp.entity.auth.UserSchoolRole;
import com.school.erp.entity.auth.Role;
import com.school.erp.entity.auth.UserRoleMapping;
import com.school.erp.repository.auth.RoleRepository;
import com.school.erp.repository.auth.UserRoleMappingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoleSyncService {

    private final RoleRepository roleRepository;
    private final UserRoleMappingRepository userRoleMappingRepository;

    @Transactional
    @org.springframework.cache.annotation.CacheEvict(value = com.school.erp.config.CacheConfig.CACHE_USER_PERMISSIONS, allEntries = true)
    public void syncUserSchoolRole(UserSchoolRole userSchoolRole) {
        if (userSchoolRole == null || userSchoolRole.getUser() == null) {
            return;
        }

        Long schoolId = userSchoolRole.getSchool() != null ? userSchoolRole.getSchool().getId() : null;
        Long userId = userSchoolRole.getUser().getId();
        String roleId = mapLegacyRoleToNewRoleId(userSchoolRole.getRole().name());

        if (roleId == null) {
            log.warn("No corresponding new role mapping for legacy role: {}", userSchoolRole.getRole());
            return;
        }

        // Check if mapping already exists
        boolean mappingExists = userRoleMappingRepository
                .findByUserIdAndIsActiveTrue(userId)
                .stream()
                .anyMatch(mapping -> java.util.Objects.equals(mapping.getSchoolId(), schoolId) && mapping.getRole().getId().equals(roleId));

        if (!mappingExists) {
            Optional<Role> roleOpt = roleRepository.findById(roleId); // Fetch by absolute ID
            if (roleOpt.isPresent()) {
                UserRoleMapping newMapping = new UserRoleMapping();
                newMapping.setSchoolId(schoolId);
                newMapping.setUser(userSchoolRole.getUser());
                newMapping.setRole(roleOpt.get());
                newMapping.setActive("ACTIVE".equalsIgnoreCase(userSchoolRole.getStatus()));
                userRoleMappingRepository.save(newMapping);
                log.info("Synchronized legacy role {} to new role mapping {} for user {}", userSchoolRole.getRole(), roleId, userId);
            } else {
                log.warn("Target role entity not found for role ID: {}", roleId);
            }
        }
    }

    private String mapLegacyRoleToNewRoleId(String legacyRole) {
        if (legacyRole == null) return null;
        return switch (legacyRole.toUpperCase()) {
            case "SUPERADMIN", "SUPER_ADMIN" -> "role_super_admin_global";
            case "ADMIN" -> "role_school_admin_global";
            case "TEACHER" -> "role_teacher_global";
            case "STUDENT" -> "role_student_global";
            case "PARENT" -> "role_parent_global";
            case "STAFF", "ACCOUNTANT" -> "role_accountant"; // Fallback for staff
            default -> null;
        };
    }
}
