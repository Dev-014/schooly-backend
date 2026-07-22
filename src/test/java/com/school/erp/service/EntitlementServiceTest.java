package com.school.erp.service;

import com.school.erp.dto.catalog.EntitlementEvaluationDto;
import com.school.erp.entity.PlatformModule;
import com.school.erp.entity.School;
import com.school.erp.entity.SubscriptionPlan;
import com.school.erp.entity.TenantEntitlementOverride;
import com.school.erp.exception.ForbiddenException;
import com.school.erp.repository.OnboardingDraftRepository;
import com.school.erp.repository.SchoolRepository;
import com.school.erp.repository.SubscriptionPlanRepository;
import com.school.erp.repository.TenantEntitlementOverrideRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EntitlementServiceTest {

    @Mock
    private SchoolRepository schoolRepo;
    @Mock
    private SubscriptionPlanRepository planRepo;
    @Mock
    private TenantEntitlementOverrideRepository overrideRepo;
    @Mock
    private OnboardingDraftRepository draftRepo;

    private EntitlementService entitlementService;
    private School testSchool;
    private SubscriptionPlan basicPlan;

    @BeforeEach
    void setUp() {
        entitlementService = new EntitlementService(schoolRepo, planRepo, overrideRepo, draftRepo);

        testSchool = new School();
        testSchool.setId(101L);
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("subscriptionPlan", "BASIC");
        testSchool.setMetadata(metadata);

        basicPlan = new SubscriptionPlan();
        basicPlan.setCode("BASIC");
        basicPlan.setName("Starter Basic");
        PlatformModule attendanceModule = new PlatformModule();
        attendanceModule.setCode("ATTENDANCE");
        PlatformModule communicationModule = new PlatformModule();
        communicationModule.setCode("COMMUNICATION");
        basicPlan.setModules(new HashSet<>(Arrays.asList(attendanceModule, communicationModule)));
    }

    @Test
    void shouldEvaluateThreeTierEntitlementsAndOverrides() {
        when(schoolRepo.findById(101L)).thenReturn(Optional.of(testSchool));
        when(planRepo.findByCode("BASIC")).thenReturn(Optional.of(basicPlan));

        TenantEntitlementOverride trialOverride = new TenantEntitlementOverride();
        trialOverride.setModuleCode("TRANSPORT");
        trialOverride.setOverrideType("TRIAL");
        trialOverride.setExpiresAt(LocalDateTime.now().plusDays(14));

        when(overrideRepo.findBySchoolId(101L)).thenReturn(Collections.singletonList(trialOverride));

        EntitlementEvaluationDto result = entitlementService.evaluateEntitlements(101L);

        assertNotNull(result);
        assertEquals("BASIC", result.getActivePlanCode());
        assertTrue(result.isTrialActive());
        assertTrue(result.getEnabledModules().contains("ATTENDANCE"));
        assertTrue(result.getEnabledModules().contains("COMMUNICATION"));
        assertTrue(result.getEnabledModules().contains("TRANSPORT"));
        assertEquals("TRIAL", result.getActiveOverrides().get("TRANSPORT"));
    }

    @Test
    void shouldAllowAccessForBundledOrCoreModules() {
        when(schoolRepo.findById(101L)).thenReturn(Optional.of(testSchool));
        when(planRepo.findByCode("BASIC")).thenReturn(Optional.of(basicPlan));
        when(overrideRepo.findBySchoolId(101L)).thenReturn(Collections.emptyList());

        assertTrue(entitlementService.hasModuleAccess(101L, "ATTENDANCE"));
        assertTrue(entitlementService.hasModuleAccess(101L, "COMMUNICATION"));
        assertFalse(entitlementService.hasModuleAccess(101L, "ANALYTICS"));
    }

    @Test
    void enforceModuleAccessShouldThrowForbiddenExceptionWhenAccessDenied() {
        when(schoolRepo.findById(101L)).thenReturn(Optional.of(testSchool));
        when(planRepo.findByCode("BASIC")).thenReturn(Optional.of(basicPlan));
        when(overrideRepo.findBySchoolId(101L)).thenReturn(Collections.emptyList());

        assertDoesNotThrow(() -> entitlementService.enforceModuleAccess(101L, "ATTENDANCE"));

        ForbiddenException exception = assertThrows(ForbiddenException.class, () -> {
            entitlementService.enforceModuleAccess(101L, "ANALYTICS");
        });
        assertTrue(exception.getMessage().contains("Access Denied (403)"));
    }
}
