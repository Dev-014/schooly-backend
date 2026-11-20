package com.school.erp.service.onboarding;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.school.erp.dto.onboarding.OnboardingDraftDTO;
import com.school.erp.dto.onboarding.OnboardingInitRequest;
import com.school.erp.dto.onboarding.OnboardingStepRequest;
import com.school.erp.entity.onboarding.DataImportError;
import com.school.erp.entity.onboarding.DataImportJob;
import com.school.erp.entity.onboarding.OnboardingDraft;
import com.school.erp.entity.superadmin.School;
import com.school.erp.exception.BadRequestException;
import com.school.erp.exception.ResourceNotFoundException;
import com.school.erp.repository.onboarding.DataImportErrorRepository;
import com.school.erp.repository.onboarding.DataImportJobRepository;
import com.school.erp.repository.onboarding.OnboardingDraftRepository;
import com.school.erp.repository.superadmin.SchoolRepository;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.school.erp.dto.onboarding.OnboardingActivationResponse;
import com.school.erp.dto.onboarding.AdminCredentialsDTO;
import com.school.erp.entity.auth.User;
import com.school.erp.entity.auth.UserRole;
import com.school.erp.entity.auth.UserSchoolRole;
import com.school.erp.entity.superadmin.SchoolSubscription;
import com.school.erp.entity.superadmin.SchoolSubscriptionInstallment;
import com.school.erp.entity.superadmin.SubscriptionPlan;
import com.school.erp.repository.auth.UserRepository;
import com.school.erp.repository.auth.UserSchoolRoleRepository;
import com.school.erp.repository.superadmin.SchoolSubscriptionRepository;
import com.school.erp.repository.superadmin.SchoolSubscriptionInstallmentRepository;
import com.school.erp.repository.superadmin.SubscriptionPlanRepository;
import com.school.erp.service.auth.RoleSyncService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.security.SecureRandom;

@Service
public class OnboardingDraftService {

    private final OnboardingDraftRepository draftRepository;
    private final SchoolRepository schoolRepository;
    private final DataImportJobRepository jobRepository;
    private final DataImportErrorRepository errorRepository;
    private final ObjectMapper objectMapper;
    private final EntityManager entityManager;
    private final UserRepository userRepository;
    private final UserSchoolRoleRepository userSchoolRoleRepository;
    private final SchoolSubscriptionRepository subscriptionRepository;
    private final SchoolSubscriptionInstallmentRepository installmentRepository;
    private final SubscriptionPlanRepository planRepository;
    private final com.school.erp.repository.superadmin.PlatformModuleRepository moduleRepository;
    private final com.school.erp.repository.superadmin.SchoolModuleAccessRepository moduleAccessRepository;
    private final RoleSyncService roleSyncService;
    private static final BCryptPasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();

    public OnboardingDraftService(OnboardingDraftRepository draftRepository,
                                  SchoolRepository schoolRepository,
                                  DataImportJobRepository jobRepository,
                                  DataImportErrorRepository errorRepository,
                                  ObjectMapper objectMapper,
                                  EntityManager entityManager,
                                  UserRepository userRepository,
                                  UserSchoolRoleRepository userSchoolRoleRepository,
                                  SchoolSubscriptionRepository subscriptionRepository,
                                  SchoolSubscriptionInstallmentRepository installmentRepository,
                                  SubscriptionPlanRepository planRepository,
                                  com.school.erp.repository.superadmin.PlatformModuleRepository moduleRepository,
                                  com.school.erp.repository.superadmin.SchoolModuleAccessRepository moduleAccessRepository,
                                  RoleSyncService roleSyncService) {
        this.draftRepository = draftRepository;
        this.schoolRepository = schoolRepository;
        this.jobRepository = jobRepository;
        this.errorRepository = errorRepository;
        this.objectMapper = objectMapper;
        this.entityManager = entityManager;
        this.userRepository = userRepository;
        this.userSchoolRoleRepository = userSchoolRoleRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.installmentRepository = installmentRepository;
        this.planRepository = planRepository;
        this.moduleRepository = moduleRepository;
        this.moduleAccessRepository = moduleAccessRepository;
        this.roleSyncService = roleSyncService;
    }


    @Transactional
    public OnboardingDraftDTO initDraft(OnboardingInitRequest request) {
        OnboardingDraft draft = new OnboardingDraft();
        draft.setStatus("DRAFT");
        draft.setCurrentStep(1);

        Map<String, Object> step1Map = new HashMap<>();
        if (request.schoolName() != null && !request.schoolName().isBlank()) {
            step1Map.put("schoolName", request.schoolName().trim());
        }
        if (request.schoolCode() != null && !request.schoolCode().isBlank()) {
            step1Map.put("schoolCode", request.schoolCode().trim());
        }
        if (request.boardType() != null && !request.boardType().isBlank()) {
            step1Map.put("boardType", request.boardType().trim());
        }
        if (request.principalEmail() != null && !request.principalEmail().isBlank()) {
            step1Map.put("principalEmail", request.principalEmail().trim());
        }
        if (request.adminPhone() != null && !request.adminPhone().isBlank()) {
            step1Map.put("adminPhone", request.adminPhone().trim());
        }
        if (request.initialMetadata() != null) {
            step1Map.putAll(request.initialMetadata());
        }

        try {
            draft.setStep1Data(objectMapper.writeValueAsString(step1Map));
        } catch (Exception e) {
            throw new BadRequestException("Failed to serialize step 1 data: " + e.getMessage());
        }

        draft = draftRepository.save(draft);

        entityManager.createNativeQuery("INSERT INTO schools (id, name, code, status) VALUES (:id, :name, :code, :status) ON CONFLICT (id) DO NOTHING")
                .setParameter("id", draft.getSchoolId())
                .setParameter("name", (request.schoolName() != null && !request.schoolName().isBlank()) ? request.schoolName() : "School " + draft.getSchoolId())
                .setParameter("code", (request.schoolCode() != null && !request.schoolCode().isBlank()) ? request.schoolCode() : "SCH-" + draft.getSchoolId() + "-" + System.currentTimeMillis() % 1000)
                .setParameter("status", "DRAFT")
                .executeUpdate();

        return toDTO(draft);
    }

    @Transactional(readOnly = true)
    public OnboardingDraftDTO getDraft(Long schoolId) {
        OnboardingDraft draft = draftRepository.findById(schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Onboarding draft not found with ID: " + schoolId));
        return toDTO(draft);
    }

    @Transactional
    public OnboardingDraftDTO saveStep(OnboardingStepRequest request) {
        OnboardingDraft draft = draftRepository.findById(request.schoolId())
                .orElseThrow(() -> new ResourceNotFoundException("Onboarding draft not found with ID: " + request.schoolId()));

        if (request.stepNumber() < 1 || request.stepNumber() > 11) {
            throw new BadRequestException("Invalid step number: " + request.stepNumber() + ". Must be between 1 and 11.");
        }

        draft.setCurrentStep(request.stepNumber());

        try {
            String jsonPayload = objectMapper.writeValueAsString(request.payload());
            switch (request.stepNumber()) {
                case 1 -> draft.setStep1Data(jsonPayload);
                case 2 -> draft.setStep2Data(jsonPayload);
                case 3 -> draft.setStep3Data(jsonPayload);
                case 4 -> draft.setStep4Data(jsonPayload);
                case 5 -> draft.setStep5Data(jsonPayload);
                case 6 -> draft.setStep6Data(jsonPayload);
                case 7 -> draft.setStep7Data(jsonPayload);
                case 8 -> draft.setStep8Data(jsonPayload);
                case 9 -> draft.setStep9Data(jsonPayload);
                case 10 -> draft.setStep10Data(jsonPayload);
                case 11 -> draft.setStep11Data(jsonPayload);
            }
        } catch (Exception e) {
            throw new BadRequestException("Failed to serialize step " + request.stepNumber() + " data: " + e.getMessage());
        }

        draft = draftRepository.save(draft);
        return toDTO(draft);
    }

    @Transactional
    public OnboardingActivationResponse activateSchool(Long schoolId) {
        OnboardingDraft draft = draftRepository.findById(schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Onboarding draft not found with ID: " + schoolId));

        // Strict Validation Gate: Check for unresolved import errors or failed records
        List<DataImportJob> jobs = jobRepository.findBySchoolIdOrderByCreatedAtDesc(schoolId);
        for (DataImportJob job : jobs) {
            if (job.getFailedRecords() != null && job.getFailedRecords() > 0) {
                throw new BadRequestException("Cannot activate school: Mandatory data import job '" + job.getCategory() + "' has unresolved validation errors (" + job.getFailedRecords() + " failed records). Please resolve all errors in Step 7 before Go-Live.");
            }
            List<DataImportError> unresolvedErrors = errorRepository.findByJobIdAndResolvedFalseOrderByErrorIdAsc(job.getJobId());
            if (!unresolvedErrors.isEmpty()) {
                throw new BadRequestException("Cannot activate school: Mandatory data import job '" + job.getCategory() + "' has " + unresolvedErrors.size() + " unresolved audit errors. Please resolve them before Go-Live.");
            }
        }

        draft.setStatus("ACTIVE");
        draft = draftRepository.save(draft);

        // Provision or update actual School record in schools table
        Map<String, Object> step1 = parseJson(draft.getStep1Data());
        Map<String, Object> step3 = parseJson(draft.getStep3Data());
        Map<String, Object> step4 = parseJson(draft.getStep4Data());
        String schoolCode = (String) step4.getOrDefault("schoolCode", "SCH-" + schoolId);
        String schoolName = (String) step4.getOrDefault("schoolName", "Onboarded School " + schoolId);

        School school = schoolRepository.findById(schoolId)
                .or(() -> schoolRepository.findByCode(schoolCode))
                .orElseGet(() -> {
                    School s = new School();
                    s.setName(schoolName);
                    s.setCode(schoolCode);
                    return s;
                });
        school.setName(schoolName);
        school.setCode(schoolCode);

        String officialEmail = firstNonBlank(
                asString(step4.get("contactEmail")),
                asString(step4.get("principalEmail")),
                asString(step4.get("primaryContactEmail"))
        );
        String officialPhone = firstNonBlank(
                asString(step4.get("contactPhone")),
                asString(step4.get("adminPhone")),
                asString(step4.get("primaryContactMobile"))
        );

        String principalEmail = firstNonBlank(
                asString(step4.get("portalLoginEmail")),
                asString(step4.get("principalEmail")),
                asString(step4.get("primaryContactEmail")),
                officialEmail
        );
        String adminPhone = firstNonBlank(
                asString(step4.get("portalLoginPhone")),
                asString(step4.get("adminPhone")),
                asString(step4.get("primaryContactMobile")),
                officialPhone
        );
        
        if (principalEmail == null || principalEmail.trim().isEmpty()) {
            throw new IllegalArgumentException("Principal Email (or Contact Email) is required for activation.");
        }
        if (adminPhone == null || adminPhone.trim().isEmpty()) {
            throw new IllegalArgumentException("Admin Phone (or Contact Phone) is required for activation.");
        }

        school.setContactEmail(officialEmail != null ? officialEmail : principalEmail);
        school.setContactPhone(officialPhone != null ? officialPhone : adminPhone);
        school.setAddress(firstNonBlank(asString(step4.get("address")), school.getAddress()));
        school.setCity(firstNonBlank(asString(step4.get("city")), school.getCity()));
        school.setState(firstNonBlank(asString(step4.get("state")), school.getState()));
        school.setPincode(firstNonBlank(asString(step4.get("pincode")), school.getPincode()));
        school.setSubdomain(firstNonBlank(asString(step4.get("subdomain")), schoolCode.toLowerCase()));
        school.setLogoUrl(firstNonBlank(asString(step4.get("logoUrl")), school.getLogoUrl()));
        school.setBannerUrl(firstNonBlank(asString(step4.get("bannerUrl")), school.getBannerUrl()));
        school.setStatus("ACTIVE");
        school.setOnboardingStatus("Step 6: Activated");

        Map<String, Object> meta = school.getMetadata() != null ? new HashMap<>(school.getMetadata()) : new HashMap<>();
        meta.put("onboardingStep", "Step 6: Activated");
        meta.put("subscriptionPlan", step1.getOrDefault("subscriptionPlan", "ENTERPRISE"));
        meta.put("boardType", step4.getOrDefault("boardType", "CBSE"));
        meta.put("udiseCode", step4.getOrDefault("udiseCode", ""));
        putIfPresent(meta, "schoolWebsite", step4.get("schoolWebsite"));
        putIfPresent(meta, "schoolType", step4.get("schoolType"));
        putIfPresent(meta, "schoolCategory", step4.get("schoolCategory"));
        putIfPresent(meta, "mediumOfInstruction", step4.get("mediumOfInstruction"));
        putIfPresent(meta, "recognitionNumber", step4.get("recognitionNumber"));
        putIfPresent(meta, "affiliationNumber", step4.get("affiliationNumber"));
        putIfPresent(meta, "schoolPanNumber", step4.get("schoolPanNumber"));
        putIfPresent(meta, "logoFileId", step4.get("logoFileId"));
        putIfPresent(meta, "bannerFileId", step4.get("bannerFileId"));
        putIfPresent(meta, "primaryContactName", step4.get("primaryContactName"));
        putIfPresent(meta, "primaryContactDesignation", step4.get("primaryContactDesignation"));
        putIfPresent(meta, "primaryContactMobile", step4.get("primaryContactMobile"));
        putIfPresent(meta, "primaryContactEmail", step4.get("primaryContactEmail"));
        putIfPresent(meta, "secondaryContactName", step4.get("secondaryContactName"));
        putIfPresent(meta, "secondaryContactDesignation", step4.get("secondaryContactDesignation"));
        putIfPresent(meta, "secondaryContactMobile", step4.get("secondaryContactMobile"));
        putIfPresent(meta, "secondaryContactEmail", step4.get("secondaryContactEmail"));
        putIfPresent(meta, "technicalContactName", step4.get("technicalContactName"));
        putIfPresent(meta, "technicalContactDesignation", step4.get("technicalContactDesignation"));
        putIfPresent(meta, "technicalContactMobile", step4.get("technicalContactMobile"));
        putIfPresent(meta, "technicalContactEmail", step4.get("technicalContactEmail"));
        putIfPresent(meta, "portalLoginSource", step4.get("portalLoginSource"));
        meta.put("portalLoginPhone", adminPhone);
        meta.put("portalLoginEmail", principalEmail);
        meta.put("billingCycle", "YEARLY");
        meta.put("paymentPlanType", step3.getOrDefault("paymentPlanType", "FULL"));
        meta.put("paymentMode", step3.getOrDefault("paymentMode", "ONLINE"));
        meta.put("gstEnabled", step3.getOrDefault("gstEnabled", true));
        meta.put("gstRate", step3.getOrDefault("gstRate", 18));
        meta.put("gstAmount", step3.getOrDefault("gstAmount", 0));
        meta.put("gstNumber", step3.getOrDefault("gstNumber", ""));
        if (step3.containsKey("customCharges")) {
            meta.put("customCharges", step3.get("customCharges"));
        }
        if (step3.containsKey("transactionId")) {
            meta.put("transactionId", step3.get("transactionId"));
        }
        java.math.BigDecimal metaGrandTotal = parseBigDecimal(step3.get("grandTotal"));
        String metaPaymentPlanType = String.valueOf(step3.getOrDefault("paymentPlanType", "FULL"));
        java.math.BigDecimal metaPaidNow = "TWO_INSTALLMENTS".equalsIgnoreCase(metaPaymentPlanType)
                ? resolveFirstInstallmentAmount(metaGrandTotal, step3.get("installment1Amount"))
                : metaGrandTotal;
        meta.put("amountPaid", metaPaidNow);
        
        // Option B: Generate Credentials
        String rawPassword = generateRandomPassword(8);
        meta.put("tempAdminPassword", rawPassword);
        school.setMetadata(meta);

        school = schoolRepository.save(school);
        
        User user = userRepository.findByPhone(adminPhone).orElse(null);
        if (user == null) {
            user = new User();
            user.setPhone(adminPhone);
            user.setName(schoolName + " Admin");
            user.setEmail(principalEmail);
            user.setPasswordHash(PASSWORD_ENCODER.encode(rawPassword));
            user.setStatus("ACTIVE");
            user = userRepository.save(user);
        } else {
            // If user already exists, update password to generated one to give access
            user.setPasswordHash(PASSWORD_ENCODER.encode(rawPassword));
            user = userRepository.save(user);
        }

        UserSchoolRole role = new UserSchoolRole();
        role.setUser(user);
        role.setSchool(school);
        role.setRole(UserRole.ADMIN);
        role.setStatus("ACTIVE");
        userSchoolRoleRepository.save(role);
        roleSyncService.syncUserSchoolRole(role);
        
        // Generate Subscription
        String planName = String.valueOf(step1.getOrDefault("subscriptionPlan", "ENTERPRISE"));
        SubscriptionPlan subscriptionPlan = resolveSubscriptionPlan(planName);
            
        if (subscriptionPlan != null) {
            // All school subscription contracts are strictly Yearly (12 months)
            String billingCycle = "YEARLY";
            int studentCount = step3.get("studentCount") instanceof Number n ? n.intValue() : 0;
            java.math.BigDecimal grandTotal = parseBigDecimal(step3.get("grandTotal"));
            String paymentPlanType = String.valueOf(step3.getOrDefault("paymentPlanType", "FULL"));
            boolean isTwoInstallments = "TWO_INSTALLMENTS".equalsIgnoreCase(paymentPlanType);

            java.math.BigDecimal firstInstallmentAmount = isTwoInstallments
                    ? resolveFirstInstallmentAmount(grandTotal, step3.get("installment1Amount"))
                    : grandTotal;
            java.math.BigDecimal remainingAmount = grandTotal.subtract(firstInstallmentAmount)
                    .max(java.math.BigDecimal.ZERO)
                    .setScale(2, java.math.RoundingMode.HALF_UP);

            subscriptionRepository.findAllBySchoolIdAndStatus(school.getId(), "ACTIVE").forEach(existing -> {
                installmentRepository.findBySubscriptionIdOrderByInstallmentNumberAsc(existing.getId())
                        .forEach(installmentRepository::delete);
                subscriptionRepository.delete(existing);
            });

            java.time.LocalDate today = java.time.LocalDate.now();
            SchoolSubscription sub = new SchoolSubscription();
            sub.setSchool(school);
            sub.setPlan(subscriptionPlan);
            sub.setBillingPeriod(billingCycle);
            sub.setTotalStudents(studentCount);
            sub.setTotalAmount(grandTotal);
            sub.setAmountPaid(firstInstallmentAmount);
            sub.setRemainingAmount(remainingAmount);
            sub.setStatus("ACTIVE");
            sub.setStartDate(today);
            sub.setEndDate(today.plusYears(1));
            sub = subscriptionRepository.save(sub);

            // Sync plan, renewal date, and payment status back onto the School entity
            school.setPlan(subscriptionPlan);
            school.setSubscriptionStart(today);
            school.setRenewalDate(sub.getEndDate());
            school.setPaymentStatus(
                    remainingAmount.compareTo(java.math.BigDecimal.ZERO) > 0
                            ? (firstInstallmentAmount.compareTo(java.math.BigDecimal.ZERO) > 0 ? "PARTIAL" : "PENDING")
                            : "PAID"
            );
            school = schoolRepository.save(school);

            SchoolSubscriptionInstallment inst1 = new SchoolSubscriptionInstallment();
            inst1.setSubscription(sub);
            inst1.setInstallmentNumber(1);
            inst1.setAmount(firstInstallmentAmount);
            inst1.setDueDate(today);
            inst1.setStatus("PAID");
            inst1.setPaidDate(today);
            installmentRepository.save(inst1);

            if (isTwoInstallments && remainingAmount.compareTo(java.math.BigDecimal.ZERO) > 0) {
                java.time.LocalDate secondDueDate = resolveSecondInstallmentDueDate(
                        step3.get("installment2DueDate"),
                        step3.get("installment2DueMonths"),
                        today
                );
                SchoolSubscriptionInstallment inst2 = new SchoolSubscriptionInstallment();
                inst2.setSubscription(sub);
                inst2.setInstallmentNumber(2);
                inst2.setAmount(remainingAmount);
                inst2.setDueDate(secondDueDate);
                inst2.setStatus("PENDING");
                installmentRepository.save(inst2);
            }
        }

        // Provision Module Access & Submodule Restrictions from Step 5 (MVP-aligned)
        Map<String, Object> step5 = parseJson(draft.getStep5Data());
        List<com.school.erp.entity.superadmin.PlatformModule> activeModules = moduleRepository.findAllByStatus("ACTIVE");

        java.util.Set<String> postMvpDisabledModules = java.util.Set.of(
                "INVENTORY",
                "DISCIPLINE",
                "LESSON_MODULE",
                "CERTIFICATES",
                "DOWNLOADS",
                "ONLINE_CLASS",
                "ONLINE_EXAMS",
                "QUESTION_PAPER"
        );

        boolean hasExplicitSelection = step5.containsKey("enabledModuleCodes") && step5.get("enabledModuleCodes") instanceof List;
        List<String> enabledCodes = new java.util.ArrayList<>();
        if (hasExplicitSelection) {
            for (Object code : (List<?>) step5.get("enabledModuleCodes")) {
                if (code instanceof String s && !s.isBlank()) {
                    enabledCodes.add(s.trim().toUpperCase());
                }
            }
        }

        Map<String, List<String>> disabledSubModulesByModule = new HashMap<>();
        if (step5.get("disabledSubModulesByModule") instanceof Map<?, ?> rawMap) {
            for (Map.Entry<?, ?> entry : rawMap.entrySet()) {
                if (entry.getKey() instanceof String modCode && entry.getValue() instanceof List<?> subList) {
                    List<String> subs = new java.util.ArrayList<>();
                    for (Object sub : subList) {
                        if (sub instanceof String s && !s.isBlank()) {
                            subs.add(s.trim());
                        }
                    }
                    disabledSubModulesByModule.put(modCode.trim().toUpperCase(), subs);
                }
            }
        }

        final School finalSchool = school;
        for (com.school.erp.entity.superadmin.PlatformModule pm : activeModules) {
            String upperCode = pm.getCode() != null ? pm.getCode().toUpperCase() : "";
            boolean isPostMvp = postMvpDisabledModules.contains(upperCode);
            boolean isInfrastructureCore = "DASHBOARD".equals(upperCode) || "FILES".equals(upperCode);
            boolean shouldEnable;
            if (isPostMvp) {
                shouldEnable = false;
            } else if (isInfrastructureCore) {
                shouldEnable = true;
            } else if (hasExplicitSelection) {
                shouldEnable = enabledCodes.contains(upperCode);
            } else {
                shouldEnable = pm.isDefault();
            }

            com.school.erp.entity.superadmin.SchoolModuleAccess access = moduleAccessRepository.findBySchoolAndModule(finalSchool, pm)
                    .orElseGet(() -> new com.school.erp.entity.superadmin.SchoolModuleAccess(finalSchool, pm));
            access.setEnabled(shouldEnable);
            access.setDisabledSubModulesList(disabledSubModulesByModule.getOrDefault(upperCode, java.util.Collections.emptyList()));
            moduleAccessRepository.save(access);
        }

        OnboardingDraftDTO draftDTO = toDTO(draft);
        AdminCredentialsDTO credentialsDTO = new AdminCredentialsDTO(adminPhone, adminPhone, rawPassword);

        return new OnboardingActivationResponse(draftDTO, credentialsDTO);
    }

    private String generateRandomPassword(int length) {
        final String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    private OnboardingDraftDTO toDTO(OnboardingDraft draft) {
        String updatedAtStr = draft.getUpdatedAt() != null
                ? draft.getUpdatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                : (draft.getCreatedAt() != null ? draft.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : "");

        return new OnboardingDraftDTO(
                draft.getSchoolId(),
                draft.getStatus(),
                draft.getCurrentStep(),
                parseJson(draft.getStep1Data()),
                parseJson(draft.getStep2Data()),
                parseJson(draft.getStep3Data()),
                parseJson(draft.getStep4Data()),
                parseJson(draft.getStep5Data()),
                parseJson(draft.getStep6Data()),
                parseJson(draft.getStep7Data()),
                parseJson(draft.getStep8Data()),
                parseJson(draft.getStep9Data()),
                parseJson(draft.getStep10Data()),
                parseJson(draft.getStep11Data()),
                updatedAtStr
        );
    }

    private Map<String, Object> parseJson(String json) {
        if (json == null || json.isBlank()) {
            return new HashMap<>();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            return new HashMap<>();
        }
    }

    private java.math.BigDecimal parseBigDecimal(Object raw) {
        if (raw == null) {
            return java.math.BigDecimal.ZERO.setScale(2, java.math.RoundingMode.HALF_UP);
        }
        try {
            return new java.math.BigDecimal(raw.toString()).setScale(2, java.math.RoundingMode.HALF_UP);
        } catch (NumberFormatException ex) {
            return java.math.BigDecimal.ZERO.setScale(2, java.math.RoundingMode.HALF_UP);
        }
    }

    private java.math.BigDecimal resolveFirstInstallmentAmount(java.math.BigDecimal grandTotal, Object explicitFirstAmount) {
        if (grandTotal.compareTo(java.math.BigDecimal.ZERO) <= 0) {
            return java.math.BigDecimal.ZERO.setScale(2, java.math.RoundingMode.HALF_UP);
        }
        if (explicitFirstAmount != null) {
            java.math.BigDecimal parsed = parseBigDecimal(explicitFirstAmount);
            if (parsed.compareTo(java.math.BigDecimal.ZERO) > 0 && parsed.compareTo(grandTotal) <= 0) {
                return parsed;
            }
        }
        return grandTotal.divide(java.math.BigDecimal.valueOf(2), 2, java.math.RoundingMode.HALF_UP);
    }

    private java.time.LocalDate resolveSecondInstallmentDueDate(Object rawDueDate, Object rawDueMonths, java.time.LocalDate today) {
        if (rawDueDate instanceof String str && !str.isBlank()) {
            try {
                return java.time.LocalDate.parse(str.trim());
            } catch (Exception ignored) {
                // fallback to months
            }
        }
        long months = 2L;
        if (rawDueMonths instanceof Number n && n.longValue() > 0) {
            months = n.longValue();
        }
        return today.plusMonths(months);
    }

    private SubscriptionPlan resolveSubscriptionPlan(String rawPlanName) {
        String normalized = rawPlanName != null ? rawPlanName.trim().toUpperCase() : "ENTERPRISE";
        List<SubscriptionPlan> activePlans = planRepository.findAllByStatus("ACTIVE");

        return activePlans.stream()
                .filter(p -> {
                    String nameUpper = p.getName() != null ? p.getName().toUpperCase() : "";
                    String codeUpper = p.getCode() != null ? p.getCode().toUpperCase() : "";
                    if (nameUpper.equals(normalized) || codeUpper.equals(normalized) || nameUpper.contains(normalized)) {
                        return true;
                    }
                    if ("BASIC".equals(normalized) && nameUpper.contains("ESSENTIAL")) return true;
                    if ("PRO".equals(normalized) && (nameUpper.contains("ADVANCED") || nameUpper.contains("PROFESSIONAL"))) return true;
                    return "ENTERPRISE".equals(normalized) && nameUpper.contains("PREMIUM");
                })
                .findFirst()
                .orElseGet(() -> planRepository.findAll().stream().findFirst().orElse(null));
    }

    private String asString(Object raw) {
        if (raw == null) return null;
        String s = raw.toString().trim();
        return s.isEmpty() ? null : s;
    }

    private String firstNonBlank(String... values) {
        for (String v : values) {
            if (v != null && !v.isBlank()) {
                return v.trim();
            }
        }
        return null;
    }

    private void putIfPresent(Map<String, Object> target, String key, Object value) {
        if (value != null && !value.toString().isBlank()) {
            target.put(key, value);
        }
    }
}
