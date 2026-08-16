package com.school.erp.service.auth;

import com.school.erp.entity.auth.PermissionDefinition;
import com.school.erp.entity.auth.Role;
import com.school.erp.entity.auth.RolePermission;
import com.school.erp.entity.auth.UserAssignment;
import com.school.erp.entity.auth.UserRoleMapping;
import com.school.erp.repository.auth.RolePermissionRepository;
import com.school.erp.repository.auth.UserAssignmentRepository;
import com.school.erp.repository.auth.UserRoleMappingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthorizationServiceTest {

    @Mock
    private UserRoleMappingRepository userRoleMappingRepository;

    @Mock
    private RolePermissionRepository rolePermissionRepository;

    @Mock
    private UserAssignmentRepository userAssignmentRepository;

    @InjectMocks
    private AuthorizationService authorizationService;

    private final Long SCHOOL_ID = 1L;
    private final Long USER_ID = 100L;
    private final Long SESSION_ID = 2026L;

    @BeforeEach
    void setUp() {
    }

    @Test
    void testHasPermission_NoRoles_ReturnsFalse() {
        when(userRoleMappingRepository.findBySchoolIdAndUserIdAndIsActiveTrue(SCHOOL_ID, USER_ID))
                .thenReturn(Collections.emptyList());

        boolean result = authorizationService.hasPermission(SCHOOL_ID, USER_ID, "attendance.edit");
        assertFalse(result);
    }

    @Test
    void testHasPermission_WithValidRole_ReturnsTrue() {
        Role teacherRole = new Role();
        teacherRole.setId("role_teacher");

        UserRoleMapping mapping = new UserRoleMapping();
        mapping.setRole(teacherRole);

        PermissionDefinition perm = new PermissionDefinition();
        perm.setPermissionKey("attendance.edit");

        RolePermission rp = new RolePermission();
        rp.setPermission(perm);

        when(userRoleMappingRepository.findBySchoolIdAndUserIdAndIsActiveTrue(SCHOOL_ID, USER_ID))
                .thenReturn(List.of(mapping));
        when(rolePermissionRepository.findBySchoolIdAndRoleId(SCHOOL_ID, "role_teacher"))
                .thenReturn(List.of(rp));

        boolean result = authorizationService.hasPermission(SCHOOL_ID, USER_ID, "attendance.edit");
        assertTrue(result);
    }

    @Test
    void testHasSectionAccess_WithSchoolScope_ReturnsTrue() {
        Role teacherRole = new Role();
        teacherRole.setId("role_principal");

        UserRoleMapping mapping = new UserRoleMapping();
        mapping.setRole(teacherRole);

        PermissionDefinition perm = new PermissionDefinition();
        perm.setPermissionKey("attendance.edit");

        RolePermission rp = new RolePermission();
        rp.setPermission(perm);
        rp.setScopeType("school");

        when(userRoleMappingRepository.findBySchoolIdAndUserIdAndIsActiveTrue(SCHOOL_ID, USER_ID))
                .thenReturn(List.of(mapping));
        when(rolePermissionRepository.findBySchoolIdAndRoleId(SCHOOL_ID, "role_principal"))
                .thenReturn(List.of(rp));

        boolean result = authorizationService.hasSectionAccess(SCHOOL_ID, USER_ID, "attendance.edit", 5L, SESSION_ID);
        assertTrue(result);
    }

    @Test
    void testHasSectionAccess_WithAssignedScope_ValidAssignment_ReturnsTrue() {
        Role teacherRole = new Role();
        teacherRole.setId("role_teacher");

        UserRoleMapping mapping = new UserRoleMapping();
        mapping.setRole(teacherRole);

        PermissionDefinition perm = new PermissionDefinition();
        perm.setPermissionKey("attendance.edit");

        RolePermission rp = new RolePermission();
        rp.setPermission(perm);
        rp.setScopeType("assigned");

        when(userRoleMappingRepository.findBySchoolIdAndUserIdAndIsActiveTrue(SCHOOL_ID, USER_ID))
                .thenReturn(List.of(mapping));
        when(rolePermissionRepository.findBySchoolIdAndRoleId(SCHOOL_ID, "role_teacher"))
                .thenReturn(List.of(rp));

        UserAssignment assignment = new UserAssignment();
        assignment.setSectionId(5L);
        assignment.setEffectiveFrom(LocalDateTime.now().minusDays(1));
        assignment.setEffectiveTo(LocalDateTime.now().plusDays(10));

        when(userAssignmentRepository.findBySchoolIdAndUserIdAndAcademicSessionIdAndIsActiveTrue(
                SCHOOL_ID, USER_ID, SESSION_ID)).thenReturn(List.of(assignment));

        boolean result = authorizationService.hasSectionAccess(SCHOOL_ID, USER_ID, "attendance.edit", 5L, SESSION_ID);
        assertTrue(result);
    }

    @Test
    void testHasSectionAccess_WithAssignedScope_InvalidAssignment_ReturnsFalse() {
        Role teacherRole = new Role();
        teacherRole.setId("role_teacher");

        UserRoleMapping mapping = new UserRoleMapping();
        mapping.setRole(teacherRole);

        PermissionDefinition perm = new PermissionDefinition();
        perm.setPermissionKey("attendance.edit");

        RolePermission rp = new RolePermission();
        rp.setPermission(perm);
        rp.setScopeType("assigned");

        when(userRoleMappingRepository.findBySchoolIdAndUserIdAndIsActiveTrue(SCHOOL_ID, USER_ID))
                .thenReturn(List.of(mapping));
        when(rolePermissionRepository.findBySchoolIdAndRoleId(SCHOOL_ID, "role_teacher"))
                .thenReturn(List.of(rp));

        UserAssignment assignment = new UserAssignment();
        assignment.setSectionId(10L); // User is assigned to section 10, not 5

        when(userAssignmentRepository.findBySchoolIdAndUserIdAndAcademicSessionIdAndIsActiveTrue(
                SCHOOL_ID, USER_ID, SESSION_ID)).thenReturn(List.of(assignment));

        boolean result = authorizationService.hasSectionAccess(SCHOOL_ID, USER_ID, "attendance.edit", 5L, SESSION_ID);
        assertFalse(result); // Should be denied because assignment doesn't match
    }

    @Test
    void testHasSectionAccess_MultipleRoles_InheritsHighestScope() {
        Role classTeacherRole = new Role();
        classTeacherRole.setId("role_class_teacher"); // scope = assigned

        Role adminRole = new Role();
        adminRole.setId("role_admin"); // scope = school

        UserRoleMapping ctMapping = new UserRoleMapping();
        ctMapping.setRole(classTeacherRole);

        UserRoleMapping adminMapping = new UserRoleMapping();
        adminMapping.setRole(adminRole);

        PermissionDefinition perm = new PermissionDefinition();
        perm.setPermissionKey("attendance.edit");

        RolePermission rp1 = new RolePermission();
        rp1.setPermission(perm);
        rp1.setScopeType("assigned");

        RolePermission rp2 = new RolePermission();
        rp2.setPermission(perm);
        rp2.setScopeType("school");

        when(userRoleMappingRepository.findBySchoolIdAndUserIdAndIsActiveTrue(SCHOOL_ID, USER_ID))
                .thenReturn(List.of(ctMapping, adminMapping));
        
        when(rolePermissionRepository.findBySchoolIdAndRoleId(SCHOOL_ID, "role_class_teacher"))
                .thenReturn(List.of(rp1));
        when(rolePermissionRepository.findBySchoolIdAndRoleId(SCHOOL_ID, "role_admin"))
                .thenReturn(List.of(rp2));

        // School scope should grant access without checking assignments
        boolean result = authorizationService.hasSectionAccess(SCHOOL_ID, USER_ID, "attendance.edit", 5L, SESSION_ID);
        assertTrue(result);
    }
}
