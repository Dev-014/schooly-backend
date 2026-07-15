package com.school.erp.service;

import com.school.erp.dto.onboarding.OnboardingRegisterRequest;
import com.school.erp.dto.onboarding.OnboardingRegisterResponse;
import com.school.erp.entity.School;
import com.school.erp.entity.User;
import com.school.erp.entity.UserSchoolRole;
import com.school.erp.exception.BadRequestException;
import com.school.erp.repository.SchoolRepository;
import com.school.erp.repository.UserRepository;
import com.school.erp.repository.UserSchoolRoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OnboardingServiceTest {

    @Mock private SchoolRepository schoolRepository;
    @Mock private UserRepository userRepository;
    @Mock private UserSchoolRoleRepository userSchoolRoleRepository;

    private OnboardingService onboardingService;

    @BeforeEach
    void setUp() {
        onboardingService = new OnboardingService(schoolRepository, userRepository, userSchoolRoleRepository);
    }

    @Test
    void registerSchool_whenCodeUnique_shouldCreateSchoolAndAdminUserWithMetadata() {
        when(schoolRepository.existsByCode("CODE01")).thenReturn(false);
        when(schoolRepository.save(any(School.class))).thenAnswer(invocation -> {
            School s = invocation.getArgument(0);
            s.setId(500L);
            return s;
        });
        when(userRepository.findByPhone("9888888888")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(600L);
            return u;
        });

        OnboardingRegisterRequest request = new OnboardingRegisterRequest(
                "Futuristic School", "CODE01", "contact@fs.com", "1234567890", "Address",
                "Principal Bob", "9888888888", "bob@fs.com", "secret",
                Map.of("customField", "value123")
        );

        OnboardingRegisterResponse response = onboardingService.registerSchool(request);

        assertNotNull(response);
        assertEquals(500L, response.schoolId());
        assertEquals("Futuristic School", response.schoolName());
        assertEquals(600L, response.adminUserId());
        assertEquals("value123", response.metadata().get("customField"));
        verify(userSchoolRoleRepository).save(any(UserSchoolRole.class));
    }

    @Test
    void registerSchool_whenCodeExists_shouldThrowBadRequestException() {
        when(schoolRepository.existsByCode("EXISTS")).thenReturn(true);
        OnboardingRegisterRequest request = new OnboardingRegisterRequest(
                "Duplicate", "EXISTS", null, null, null, "Admin", "9000000000", null, null, null
        );

        assertThrows(BadRequestException.class, () -> onboardingService.registerSchool(request));
        verify(schoolRepository, never()).save(any());
    }
}
