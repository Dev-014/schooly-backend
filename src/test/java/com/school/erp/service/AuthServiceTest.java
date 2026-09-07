package com.school.erp.service;

import com.school.erp.dto.auth.LoginVerifyResponse;
import com.school.erp.dto.auth.OtpVerifyRequest;
import com.school.erp.entity.AuthSession;
import com.school.erp.entity.School;
import com.school.erp.entity.User;
import com.school.erp.entity.UserRole;
import com.school.erp.entity.auth.Role;
import com.school.erp.entity.auth.RoleArchetype;
import com.school.erp.entity.auth.UserRoleMapping;
import com.school.erp.exception.UnauthorizedException;
import com.school.erp.repository.AuthSessionRepository;
import com.school.erp.repository.SchoolRepository;
import com.school.erp.repository.StudentParentRepository;
import com.school.erp.repository.StudentRepository;
import com.school.erp.repository.UserRepository;
import com.school.erp.repository.auth.UserRoleMappingRepository;
import com.school.erp.security.JwtUtil;
import com.school.erp.service.auth.AuthorizationService;
import com.school.erp.service.auth.RoleSyncService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private UserRoleMappingRepository userRoleMappingRepository;
    @Mock private AuthSessionRepository authSessionRepository;
    @Mock private StudentParentRepository studentParentRepository;
    @Mock private SchoolRepository schoolRepository;
    @Mock private StudentRepository studentRepository;
    @Mock private JwtUtil jwtUtil;
    @Mock private AuthorizationService authorizationService;
    @Mock private RoleSyncService roleSyncService;
    @Mock private com.school.erp.repository.auth.RolePermissionRepository rolePermissionRepository;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(
                userRepository, userRoleMappingRepository, authSessionRepository,
                studentParentRepository, schoolRepository, studentRepository, jwtUtil, authorizationService, roleSyncService, rolePermissionRepository
        );
    }

    @Test
    void verifyOtp_withDummyOtp1111AndSingleSchool_shouldGenerateTokensAndReturnNoSchoolSelection() {
        User user = new User();
        user.setId(1L);
        user.setPhone("9999999999");
        user.setName("Test Admin");

        School school = new School();
        school.setId(10L);
        school.setName("Test School");
        school.setCode("TST");
        school.setStatus("ACTIVE");

        Role roleEntity = new Role();
        roleEntity.setId("role_school_admin_global");
        roleEntity.setArchetype(RoleArchetype.SCHOOL_ADMIN);

        UserRoleMapping role = new UserRoleMapping();
        role.setUser(user);
        role.setSchoolId(10L);
        role.setRole(roleEntity);
        role.setActive(true);

        when(userRepository.findByPhone("9999999999")).thenReturn(Optional.of(user));
        when(userRoleMappingRepository.findByUserIdAndIsActiveTrue(1L)).thenReturn(List.of(role));
        when(schoolRepository.findById(10L)).thenReturn(Optional.of(school));
        when(jwtUtil.generateAccessToken(eq(1L), eq(10L), eq(UserRole.ADMIN))).thenReturn("token-access");
        when(jwtUtil.generateRefreshToken(eq(1L), eq(10L), eq(UserRole.ADMIN))).thenReturn("token-refresh");

        OtpVerifyRequest request = new OtpVerifyRequest("9999999999", "1111");
        LoginVerifyResponse response = authService.verifyOtp(request, null);

        assertNotNull(response);
        assertEquals(1L, response.userId());
        assertEquals("ADMIN", response.primaryRole());
        assertFalse(response.requiresSchoolSelection());
        assertEquals("token-access", response.accessToken());
        verify(authSessionRepository).save(any(AuthSession.class));
    }

    @Test
    void verifyOtp_withMultiSchoolAndNotSuperAdmin_shouldSetRequiresSchoolSelectionTrueAndNullTokens() {
        User user = new User();
        user.setId(2L);
        user.setPhone("5555555555");

        School school1 = new School(); school1.setId(10L); school1.setCode("S1"); school1.setName("School 1");
        School school2 = new School(); school2.setId(20L); school2.setCode("S2"); school2.setName("School 2");

        Role roleTeacher = new Role(); roleTeacher.setArchetype(RoleArchetype.STAFF);
        Role roleAdmin = new Role(); roleAdmin.setArchetype(RoleArchetype.SCHOOL_ADMIN);

        UserRoleMapping role1 = new UserRoleMapping(); role1.setUser(user); role1.setSchoolId(10L); role1.setRole(roleTeacher); role1.setActive(true);
        UserRoleMapping role2 = new UserRoleMapping(); role2.setUser(user); role2.setSchoolId(20L); role2.setRole(roleAdmin); role2.setActive(true);

        when(userRepository.findByPhone("5555555555")).thenReturn(Optional.of(user));
        when(userRoleMappingRepository.findByUserIdAndIsActiveTrue(2L)).thenReturn(List.of(role1, role2));
        when(schoolRepository.findById(10L)).thenReturn(Optional.of(school1));
        when(schoolRepository.findById(20L)).thenReturn(Optional.of(school2));

        OtpVerifyRequest request = new OtpVerifyRequest("5555555555", "1111");
        LoginVerifyResponse response = authService.verifyOtp(request, null);

        assertNotNull(response);
        assertTrue(response.requiresSchoolSelection());
        assertNull(response.accessToken());
        verify(authSessionRepository, never()).save(any());
    }

    @Test
    void verifyOtp_withInvalidOtp_shouldThrowUnauthorizedException() {
        OtpVerifyRequest request = new OtpVerifyRequest("9999999999", "1234");
        assertThrows(UnauthorizedException.class, () -> authService.verifyOtp(request, null));
    }

    @Test
    void loginOrSignup_existingUser_returnsUserWithSchools() {
        User user = new User();
        user.setId(1L);
        user.setPhone("9999999999");
        user.setName("Test Admin");

        School school = new School();
        school.setId(10L);
        school.setName("Greenwood Academy");
        school.setCode("GWA");

        Role roleEntity = new Role();
        roleEntity.setArchetype(RoleArchetype.SCHOOL_ADMIN);

        UserRoleMapping role = new UserRoleMapping();
        role.setUser(user);
        role.setSchoolId(10L);
        role.setRole(roleEntity);
        role.setActive(true);

        when(userRepository.findByPhone("9999999999")).thenReturn(Optional.of(user));
        when(userRoleMappingRepository.findByUserIdAndIsActiveTrue(1L)).thenReturn(List.of(role));
        when(schoolRepository.findById(10L)).thenReturn(Optional.of(school));

        var response = authService.loginOrSignup("9999999999");
        assertNotNull(response);
        assertEquals(1L, response.id());
        assertFalse(response.newlyCreated());
        assertEquals(1, response.schools().size());
        assertEquals("Greenwood Academy", response.schools().get(0).schoolName());
    }

    @Test
    void loginOrSignup_newUser_createsUserAndReturnsEmptySchools() {
        User newUser = new User();
        newUser.setId(100L);
        newUser.setPhone("1234567890");
        newUser.setStatus("ACTIVE");

        when(userRepository.findByPhone("1234567890")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(newUser);
        when(userRoleMappingRepository.findByUserIdAndIsActiveTrue(100L)).thenReturn(List.of());

        var response = authService.loginOrSignup("1234567890");
        assertNotNull(response);
        assertEquals(100L, response.id());
        assertTrue(response.newlyCreated());
        assertTrue(response.schools().isEmpty());
    }
}
