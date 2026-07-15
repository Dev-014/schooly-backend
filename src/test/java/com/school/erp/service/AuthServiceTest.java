package com.school.erp.service;

import com.school.erp.dto.auth.LoginVerifyResponse;
import com.school.erp.dto.auth.OtpVerifyRequest;
import com.school.erp.entity.AuthSession;
import com.school.erp.entity.School;
import com.school.erp.entity.User;
import com.school.erp.entity.UserRole;
import com.school.erp.entity.UserSchoolRole;
import com.school.erp.exception.UnauthorizedException;
import com.school.erp.repository.AuthSessionRepository;
import com.school.erp.repository.StudentParentRepository;
import com.school.erp.repository.StudentRepository;
import com.school.erp.repository.UserRepository;
import com.school.erp.repository.UserSchoolRoleRepository;
import com.school.erp.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
    @Mock private UserSchoolRoleRepository userSchoolRoleRepository;
    @Mock private AuthSessionRepository authSessionRepository;
    @Mock private StudentParentRepository studentParentRepository;
    @Mock private StudentRepository studentRepository;
    @Mock private JwtUtil jwtUtil;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(
                userRepository, userSchoolRoleRepository, authSessionRepository,
                studentParentRepository, studentRepository, jwtUtil
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

        UserSchoolRole role = new UserSchoolRole();
        role.setUser(user);
        role.setSchool(school);
        role.setRole(UserRole.ADMIN);

        when(userRepository.findByPhone("9999999999")).thenReturn(Optional.of(user));
        when(userSchoolRoleRepository.findByUserIdAndStatusIgnoreCase(1L, "ACTIVE")).thenReturn(List.of(role));
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

        UserSchoolRole role1 = new UserSchoolRole(); role1.setUser(user); role1.setSchool(school1); role1.setRole(UserRole.TEACHER);
        UserSchoolRole role2 = new UserSchoolRole(); role2.setUser(user); role2.setSchool(school2); role2.setRole(UserRole.ADMIN);

        when(userRepository.findByPhone("5555555555")).thenReturn(Optional.of(user));
        when(userSchoolRoleRepository.findByUserIdAndStatusIgnoreCase(2L, "ACTIVE")).thenReturn(List.of(role1, role2));

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
}
