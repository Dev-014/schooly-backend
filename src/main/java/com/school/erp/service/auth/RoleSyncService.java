package com.school.erp.service.auth;

import com.school.erp.entity.UserSchoolRole;
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
    public void syncUserSchoolRole(UserSchoolRole userSchoolRole) {
        if (userSchoolRole == null || userSchoolRole.getUser() == null || userSchoolRole.getSchool() == null) {
            return;
        }

        Long schoolId = userSchoolRole.getSchool().getId();
        Long userId = userSchoolRole.getUser().getId();
        String roleId = mapLegacyRoleToNewRoleId(userSchoolRole.getRole().name());

        if (roleId == null) {
            log.warn("No corresponding new role mapping for legacy role: {}", userSchoolRole.getRole());
            return;
        }

        // Check if mapping already exists
        boolean mappingExists = userRoleMappingRepository
                .findBySchoolIdAndUserIdAndIsActiveTrue(schoolId, userId)
                .stream()
                .anyMatch(mapping -> mapping.getRole().getId().equals(roleId));

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
            case "ADMIN", "SUPERADMIN", "SUPER_ADMIN" -> "role_school_admin";
            case "TEACHER" -> "role_teacher";
            case "STAFF", "ACCOUNTANT" -> "role_accountant"; // Fallback for staff
            default -> null; // Students/Parents don't map to these roles yet unless specified
        };
    }
}
