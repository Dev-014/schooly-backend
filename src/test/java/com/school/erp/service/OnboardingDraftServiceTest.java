package com.school.erp.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.school.erp.dto.onboarding.OnboardingDraftDTO;
import com.school.erp.entity.onboarding.DataImportError;
import com.school.erp.entity.onboarding.DataImportJob;
import com.school.erp.entity.onboarding.OnboardingDraft;
import com.school.erp.entity.superadmin.School;
import com.school.erp.exception.BadRequestException;
import com.school.erp.repository.onboarding.DataImportErrorRepository;
import com.school.erp.repository.onboarding.DataImportJobRepository;
import com.school.erp.repository.onboarding.OnboardingDraftRepository;
import com.school.erp.repository.superadmin.SchoolRepository;
import com.school.erp.repository.auth.UserRepository;
import com.school.erp.repository.auth.UserSchoolRoleRepository;
import com.school.erp.repository.superadmin.SchoolSubscriptionRepository;
import com.school.erp.repository.superadmin.SchoolSubscriptionInstallmentRepository;
import com.school.erp.repository.superadmin.SubscriptionPlanRepository;
import com.school.erp.repository.superadmin.PlatformModuleRepository;
import com.school.erp.repository.superadmin.SchoolModuleAccessRepository;
import com.school.erp.service.auth.RoleSyncService;
import com.school.erp.service.onboarding.OnboardingDraftService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import com.school.erp.dto.onboarding.OnboardingActivationResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OnboardingDraftServiceTest {

    @Mock private OnboardingDraftRepository draftRepository;
    @Mock private SchoolRepository schoolRepository;
    @Mock private DataImportJobRepository jobRepository;
    @Mock private DataImportErrorRepository errorRepository;
    @Mock private EntityManager entityManager;
    @Mock private UserRepository userRepository;
    @Mock private UserSchoolRoleRepository userSchoolRoleRepository;
    @Mock private SchoolSubscriptionRepository subscriptionRepository;
    @Mock private SchoolSubscriptionInstallmentRepository installmentRepository;
    @Mock private SubscriptionPlanRepository planRepository;
    @Mock private PlatformModuleRepository moduleRepository;
    @Mock private SchoolModuleAccessRepository moduleAccessRepository;
    @Mock private RoleSyncService roleSyncService;

    private ObjectMapper objectMapper;
    private OnboardingDraftService draftService;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        draftService = new OnboardingDraftService(
                draftRepository, 
                schoolRepository, 
                jobRepository, 
                errorRepository, 
                objectMapper, 
                entityManager, 
                userRepository, 
                userSchoolRoleRepository, 
                subscriptionRepository, 
                installmentRepository, 
                planRepository, 
                moduleRepository, 
                moduleAccessRepository,
                roleSyncService
        );
    }

    @Test
    void activateSchool_whenNoImportErrors_shouldActivateAndStorePlanMetadata() throws Exception {
        OnboardingDraft draft = new OnboardingDraft();
        draft.setSchoolId(101L);
        draft.setStatus("DRAFT");
        draft.setStep1Data(objectMapper.writeValueAsString(new HashMap<String, Object>() {{
            put("subscriptionPlan", "PRO");
        }}));
        draft.setStep4Data(objectMapper.writeValueAsString(new HashMap<String, Object>() {{
            put("schoolName", "Greenwood Academy");
            put("schoolCode", "GA-101");
            put("contactEmail", "contact@greenwood.edu");
            put("contactPhone", "1234567890");
        }}));

        when(draftRepository.findById(101L)).thenReturn(Optional.of(draft));
        when(draftRepository.save(any(OnboardingDraft.class))).thenAnswer(i -> i.getArgument(0));
        when(jobRepository.findBySchoolIdOrderByCreatedAtDesc(101L)).thenReturn(Collections.emptyList());
        when(schoolRepository.findById(101L)).thenReturn(Optional.empty());
        when(schoolRepository.findByCode("GA-101")).thenReturn(Optional.empty());
        when(schoolRepository.save(any(School.class))).thenAnswer(i -> i.getArgument(0));
        when(userRepository.findByPhone(any())).thenReturn(Optional.empty());
        when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(userSchoolRoleRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(planRepository.findAllByStatus("ACTIVE")).thenReturn(Collections.emptyList());
        when(planRepository.findAll()).thenReturn(Collections.emptyList());

        OnboardingActivationResponse result = draftService.activateSchool(101L);

        assertNotNull(result);
        assertEquals("ACTIVE", result.draft().status());
        assertNotNull(result.adminCredentials());
        verify(schoolRepository).save(argThat(school ->
                "Greenwood Academy".equals(school.getName()) &&
                "ACTIVE".equals(school.getStatus()) &&
                "PRO".equals(school.getMetadata().get("subscriptionPlan"))
        ));
    }

    @Test
    void activateSchool_whenFailedRecordsExist_shouldThrowBadRequestException() {
        OnboardingDraft draft = new OnboardingDraft();
        draft.setSchoolId(102L);
        when(draftRepository.findById(102L)).thenReturn(Optional.of(draft));

        DataImportJob job = new DataImportJob();
        job.setJobId(55L);
        job.setCategory("students");
        job.setFailedRecords(3);
        when(jobRepository.findBySchoolIdOrderByCreatedAtDesc(102L)).thenReturn(List.of(job));

        BadRequestException ex = assertThrows(BadRequestException.class, () -> draftService.activateSchool(102L));
        assertTrue(ex.getMessage().contains("Mandatory data import job 'students' has unresolved validation errors (3 failed records)"));
        verify(draftRepository, never()).save(any());
        verify(schoolRepository, never()).save(any());
    }

    @Test
    void activateSchool_whenUnresolvedAuditErrorsExist_shouldThrowBadRequestException() {
        OnboardingDraft draft = new OnboardingDraft();
        draft.setSchoolId(103L);
        when(draftRepository.findById(103L)).thenReturn(Optional.of(draft));

        DataImportJob job = new DataImportJob();
        job.setJobId(56L);
        job.setCategory("teachers");
        job.setFailedRecords(0);
        when(jobRepository.findBySchoolIdOrderByCreatedAtDesc(103L)).thenReturn(List.of(job));

        DataImportError error = new DataImportError();
        error.setErrorId(99L);
        error.setResolved(false);
        when(errorRepository.findByJobIdAndResolvedFalseOrderByErrorIdAsc(56L)).thenReturn(List.of(error));

        BadRequestException ex = assertThrows(BadRequestException.class, () -> draftService.activateSchool(103L));
        assertTrue(ex.getMessage().contains("Mandatory data import job 'teachers' has 1 unresolved audit errors"));
        verify(draftRepository, never()).save(any());
        verify(schoolRepository, never()).save(any());
    }

    @Test
    void activateSchool_withTwoInstallmentsAndCustomCharges_shouldCreatePaidAndPendingInstallments() throws Exception {
        OnboardingDraft draft = new OnboardingDraft();
        draft.setSchoolId(205L);
        draft.setStatus("DRAFT");
        draft.setStep1Data(objectMapper.writeValueAsString(new HashMap<String, Object>() {{
            put("subscriptionPlan", "ENTERPRISE");
            put("pricePerStudent", 30);
        }}));
        draft.setStep3Data(objectMapper.writeValueAsString(new HashMap<String, Object>() {{
            put("selectedPlan", "ENTERPRISE");
            put("billingCycle", "YEARLY");
            put("studentCount", 100);
            put("planAmount", 36000);
            put("gstEnabled", false);
            put("gstRate", 0);
            put("gstAmount", 0);
            put("grandTotal", 41000);
            put("paymentPlanType", "TWO_INSTALLMENTS");
            put("installment1Amount", 20500);
            put("installment2Amount", 20500);
            put("installment2DueMonths", 2);
            put("installment2DueDate", "2027-01-19");
            put("paymentMode", "UPI");
            put("gstNumber", "27AADCB2230M1Z2");
            put("customCharges", List.of(new HashMap<String, Object>() {{
                put("id", "c1");
                put("label", "Setup Fee");
                put("type", "ADDITION");
                put("amount", 5000);
            }}));
        }}));
        draft.setStep4Data(objectMapper.writeValueAsString(new HashMap<String, Object>() {{
            put("schoolName", "Apex Public School");
            put("schoolCode", "APS-205");
            put("principalEmail", "principal@apex.edu");
            put("adminPhone", "9876543210");
        }}));

        com.school.erp.entity.superadmin.SubscriptionPlan premiumPlan = new com.school.erp.entity.superadmin.SubscriptionPlan();
        premiumPlan.setId(3L);
        premiumPlan.setName("Premium Plan");
        premiumPlan.setCode("ENTERPRISE");

        when(draftRepository.findById(205L)).thenReturn(Optional.of(draft));
        when(draftRepository.save(any(OnboardingDraft.class))).thenAnswer(i -> i.getArgument(0));
        when(jobRepository.findBySchoolIdOrderByCreatedAtDesc(205L)).thenReturn(Collections.emptyList());
        when(schoolRepository.findById(205L)).thenReturn(Optional.empty());
        when(schoolRepository.findByCode("APS-205")).thenReturn(Optional.empty());
        when(schoolRepository.save(any(School.class))).thenAnswer(i -> {
            School s = i.getArgument(0);
            s.setId(205L);
            return s;
        });
        when(userRepository.findByPhone("9876543210")).thenReturn(Optional.empty());
        when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(userSchoolRoleRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(planRepository.findAllByStatus("ACTIVE")).thenReturn(List.of(premiumPlan));
        when(subscriptionRepository.findAllBySchoolIdAndStatus(205L, "ACTIVE")).thenReturn(Collections.emptyList());
        when(subscriptionRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(installmentRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        OnboardingActivationResponse result = draftService.activateSchool(205L);

        assertNotNull(result);
        assertEquals("ACTIVE", result.draft().status());

        verify(subscriptionRepository).save(argThat(sub ->
                sub.getTotalStudents() == 100 &&
                sub.getTotalAmount().compareTo(new java.math.BigDecimal("41000.00")) == 0 &&
                sub.getAmountPaid().compareTo(new java.math.BigDecimal("20500.00")) == 0 &&
                sub.getRemainingAmount().compareTo(new java.math.BigDecimal("20500.00")) == 0
        ));

        verify(installmentRepository).save(argThat(inst ->
                inst.getInstallmentNumber() == 1 &&
                "PAID".equals(inst.getStatus()) &&
                inst.getAmount().compareTo(new java.math.BigDecimal("20500.00")) == 0
        ));
        verify(installmentRepository).save(argThat(inst ->
                inst.getInstallmentNumber() == 2 &&
                "PENDING".equals(inst.getStatus()) &&
                inst.getAmount().compareTo(new java.math.BigDecimal("20500.00")) == 0 &&
                java.time.LocalDate.of(2027, 1, 19).equals(inst.getDueDate())
        ));
    }

    @Test
    void activateSchool_withBrandingAndPortalLoginChoice_shouldPersistLogoBannerAndChosenAdminPhone() throws Exception {
        OnboardingDraft draft = new OnboardingDraft();
        draft.setSchoolId(306L);
        draft.setStatus("DRAFT");
        draft.setStep1Data(objectMapper.writeValueAsString(new HashMap<String, Object>() {{
            put("subscriptionPlan", "PRO");
        }}));
        draft.setStep4Data(objectMapper.writeValueAsString(new HashMap<String, Object>() {{
            put("schoolName", "Horizon Global School");
            put("schoolCode", "HGS");
            put("contactEmail", "info@horizon.edu.in");
            put("contactPhone", "9999900001");
            put("address", "Sector 21, Noida");
            put("city", "Noida");
            put("state", "Uttar Pradesh");
            put("pincode", "201301");
            put("logoUrl", "https://cdn.schooly.io/schools/306/logo.png");
            put("bannerUrl", "https://cdn.schooly.io/schools/306/banner.jpg");
            put("schoolWebsite", "https://horizon.edu.in");
            put("schoolType", "Private Unaided");
            put("schoolCategory", "Senior Secondary (I-XII)");
            put("mediumOfInstruction", "English");
            put("recognitionNumber", "UP-REC-2026-101");
            put("affiliationNumber", "2130999");
            put("udiseCode", "09100100203");
            put("schoolPanNumber", "AAACH1234F");
            put("primaryContactName", "Dr. Meera Verma");
            put("primaryContactDesignation", "Principal");
            put("primaryContactMobile", "9811122233");
            put("primaryContactEmail", "principal@horizon.edu.in");
            put("technicalContactName", "Rohan IT");
            put("technicalContactMobile", "9822233344");
            put("technicalContactEmail", "it@horizon.edu.in");
            put("portalLoginSource", "TECHNICAL_CONTACT");
            put("portalLoginPhone", "9822233344");
            put("portalLoginEmail", "it@horizon.edu.in");
            put("adminPhone", "9822233344");
            put("principalEmail", "it@horizon.edu.in");
        }}));

        when(draftRepository.findById(306L)).thenReturn(Optional.of(draft));
        when(draftRepository.save(any(OnboardingDraft.class))).thenAnswer(i -> i.getArgument(0));
        when(jobRepository.findBySchoolIdOrderByCreatedAtDesc(306L)).thenReturn(Collections.emptyList());
        when(schoolRepository.findById(306L)).thenReturn(Optional.empty());
        when(schoolRepository.findByCode("HGS")).thenReturn(Optional.empty());
        when(schoolRepository.save(any(School.class))).thenAnswer(i -> {
            School s = i.getArgument(0);
            s.setId(306L);
            return s;
        });
        when(userRepository.findByPhone("9822233344")).thenReturn(Optional.empty());
        when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(userSchoolRoleRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(planRepository.findAllByStatus("ACTIVE")).thenReturn(Collections.emptyList());
        when(planRepository.findAll()).thenReturn(Collections.emptyList());

        OnboardingActivationResponse result = draftService.activateSchool(306L);

        assertEquals("9822233344", result.adminCredentials().phone());
        verify(schoolRepository).save(argThat(s ->
                "https://cdn.schooly.io/schools/306/logo.png".equals(s.getLogoUrl()) &&
                "https://cdn.schooly.io/schools/306/banner.jpg".equals(s.getBannerUrl()) &&
                "Noida".equals(s.getCity()) &&
                "Uttar Pradesh".equals(s.getState()) &&
                "201301".equals(s.getPincode()) &&
                "info@horizon.edu.in".equals(s.getContactEmail()) &&
                "9999900001".equals(s.getContactPhone()) &&
                "AAACH1234F".equals(s.getMetadata().get("schoolPanNumber")) &&
                "TECHNICAL_CONTACT".equals(s.getMetadata().get("portalLoginSource"))
        ));
    }

    @Test
    void activateSchool_shouldEnforceMvpModulePolicyAndPersistDisabledSubModules() throws Exception {
        OnboardingDraft draft = new OnboardingDraft();
        draft.setSchoolId(408L);
        draft.setStatus("DRAFT");
        draft.setStep1Data(objectMapper.writeValueAsString(new HashMap<String, Object>() {{
            put("subscriptionPlan", "PRO");
        }}));
        draft.setStep4Data(objectMapper.writeValueAsString(new HashMap<String, Object>() {{
            put("schoolName", "MVP School");
            put("schoolCode", "MVPS");
            put("principalEmail", "admin@mvps.edu");
            put("adminPhone", "9800011122");
        }}));
        draft.setStep5Data(objectMapper.writeValueAsString(new HashMap<String, Object>() {{
            put("enabledModuleCodes", List.of("STUDENTS", "INVENTORY")); // INVENTORY is post-MVP and must remain disabled
            put("disabledSubModulesByModule", new HashMap<String, Object>() {{
                put("STUDENTS", List.of("online-admission", "student-referral"));
            }});
        }}));

        com.school.erp.entity.superadmin.PlatformModule studentsMod = new com.school.erp.entity.superadmin.PlatformModule();
        studentsMod.setId(6L);
        studentsMod.setCode("STUDENTS");
        studentsMod.setDefault(true);

        com.school.erp.entity.superadmin.PlatformModule inventoryMod = new com.school.erp.entity.superadmin.PlatformModule();
        inventoryMod.setId(24L);
        inventoryMod.setCode("INVENTORY");
        inventoryMod.setDefault(false);

        when(draftRepository.findById(408L)).thenReturn(Optional.of(draft));
        when(draftRepository.save(any(OnboardingDraft.class))).thenAnswer(i -> i.getArgument(0));
        when(jobRepository.findBySchoolIdOrderByCreatedAtDesc(408L)).thenReturn(Collections.emptyList());
        when(schoolRepository.findById(408L)).thenReturn(Optional.empty());
        when(schoolRepository.findByCode("MVPS")).thenReturn(Optional.empty());
        when(schoolRepository.save(any(School.class))).thenAnswer(i -> {
            School s = i.getArgument(0);
            s.setId(408L);
            return s;
        });
        when(userRepository.findByPhone("9800011122")).thenReturn(Optional.empty());
        when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(userSchoolRoleRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(planRepository.findAllByStatus("ACTIVE")).thenReturn(Collections.emptyList());
        when(planRepository.findAll()).thenReturn(Collections.emptyList());
        when(moduleRepository.findAllByStatus("ACTIVE")).thenReturn(List.of(studentsMod, inventoryMod));
        when(moduleAccessRepository.findBySchoolAndModule(any(), any())).thenReturn(Optional.empty());
        when(moduleAccessRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        draftService.activateSchool(408L);

        verify(moduleAccessRepository).save(argThat(access ->
                "STUDENTS".equals(access.getModule().getCode()) &&
                Boolean.TRUE.equals(access.getEnabled()) &&
                List.of("online-admission", "student-referral").equals(access.getDisabledSubModulesList())
        ));
        verify(moduleAccessRepository).save(argThat(access ->
                "INVENTORY".equals(access.getModule().getCode()) &&
                Boolean.FALSE.equals(access.getEnabled())
        ));
    }
}
