package com.school.erp.service.auth;

import com.school.erp.entity.auth.RolePermission;
import com.school.erp.entity.auth.UserRoleMapping;
import com.school.erp.entity.auth.UserAssignment;
import com.school.erp.repository.auth.RolePermissionRepository;
import com.school.erp.repository.auth.UserAssignmentRepository;
import com.school.erp.repository.auth.UserRoleMappingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthorizationService {

    private final UserRoleMappingRepository userRoleMappingRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final UserAssignmentRepository userAssignmentRepository;

    /**
     * Checks if a user has a specific permission within a school context.
     * Currently evaluates if ANY active role grants the permission.
     */
    @Transactional(readOnly = true)
    public boolean hasPermission(Long schoolId, Long userId, String permissionKey) {
        if (schoolId == null || userId == null || permissionKey == null) {
            return false;
        }

        List<RolePermission> effective = getEffectivePermissions(schoolId, userId);
        return effective.stream().anyMatch(rp -> rp.getPermission().getPermissionKey().equals(permissionKey));
    }

    /**
     * Gets all active permissions across all active roles for a user in a school.
     */
    @Transactional(readOnly = true)
    public List<RolePermission> getEffectivePermissions(Long schoolId, Long userId) {
        List<RolePermission> effectivePermissions = new ArrayList<>();
        List<UserRoleMapping> roleMappings = userRoleMappingRepository
                .findBySchoolIdAndUserIdAndIsActiveTrue(schoolId, userId);

        for (UserRoleMapping mapping : roleMappings) {
            List<RolePermission> permissions = rolePermissionRepository
                    .findBySchoolIdAndRoleId(schoolId, mapping.getRole().getId());
            effectivePermissions.addAll(permissions);
        }
        return effectivePermissions;
    }

    /**
     * Validates if the user has access to a specific section based on their effective scopes and assignments.
     */
    @Transactional(readOnly = true)
    public boolean hasSectionAccess(Long schoolId, Long userId, String permissionKey, Long sectionId, Long currentAcademicSessionId) {
        List<RolePermission> effective = getEffectivePermissions(schoolId, userId);
        
        boolean hasPermission = false;
        boolean requiresAssignment = true;
        
        for (RolePermission rp : effective) {
            if (rp.getPermission().getPermissionKey().equals(permissionKey)) {
                hasPermission = true;
                if ("school".equalsIgnoreCase(rp.getScopeType())) {
                    return true; // School scope grants access to everything in the school
                }
                if ("assigned".equalsIgnoreCase(rp.getScopeType()) || "section".equalsIgnoreCase(rp.getScopeType())) {
                    requiresAssignment = true;
                }
            }
        }
        
        if (!hasPermission) return false;
        if (!requiresAssignment) return true;

        // Check if user has an active assignment for this section
        LocalDateTime now = LocalDateTime.now();
        List<UserAssignment> assignments = userAssignmentRepository.findBySchoolIdAndUserIdAndAcademicSessionIdAndIsActiveTrue(
                schoolId, userId, currentAcademicSessionId
        );
        
        return assignments.stream()
                .filter(a -> a.getSectionId() != null && a.getSectionId().equals(sectionId))
                .anyMatch(a -> (a.getEffectiveFrom() == null || !a.getEffectiveFrom().isAfter(now)) &&
                               (a.getEffectiveTo() == null || !a.getEffectiveTo().isBefore(now)));
    }
}
