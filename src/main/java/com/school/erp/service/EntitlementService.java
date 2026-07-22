package com.school.erp.service;

import com.school.erp.dto.catalog.EntitlementEvaluationDto;
import com.school.erp.entity.*;
import com.school.erp.exception.ResourceNotFoundException;
import com.school.erp.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class EntitlementService {

    private final SchoolRepository schoolRepo;
    private final SubscriptionPlanRepository planRepo;
    private final TenantEntitlementOverrideRepository overrideRepo;
    private final OnboardingDraftRepository draftRepo;

    public EntitlementService(SchoolRepository schoolRepo,
                              SubscriptionPlanRepository planRepo,
                              TenantEntitlementOverrideRepository overrideRepo,
                              OnboardingDraftRepository draftRepo) {
        this.schoolRepo = schoolRepo;
        this.planRepo = planRepo;
        this.overrideRepo = overrideRepo;
        this.draftRepo = draftRepo;
    }

    @Transactional(readOnly = true)
    public EntitlementEvaluationDto evaluateEntitlements(Long schoolId) {
        School school = schoolRepo.findById(schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("School not found with ID: " + schoolId));

        // Determine active plan code (check School metadata or OnboardingDraft)
        String planCode = "GROWTH"; // default safe fallback if not set
        if (school.getMetadata() != null && school.getMetadata().get("subscriptionPlan") != null) {
            planCode = school.getMetadata().get("subscriptionPlan").toString().toUpperCase();
        } else {
            Optional<OnboardingDraft> draftOpt = draftRepo.findById(schoolId);
            if (draftOpt.isPresent() && draftOpt.get().getStep3Data() != null) {
                String s3 = draftOpt.get().getStep3Data();
                if (s3.contains("FREE")) planCode = "FREE";
                else if (s3.contains("GROWTH")) planCode = "GROWTH";
                else if (s3.contains("PREMIUM")) planCode = "PREMIUM";
                else if (s3.contains("ENTERPRISE")) planCode = "ENTERPRISE";
            }
        }

        SubscriptionPlan plan = planRepo.findByCode(planCode)
                .orElseGet(() -> planRepo.findAllByStatus("ACTIVE").stream()
                        .filter(p -> p.getName().equalsIgnoreCase("Pro Growth"))
                        .findFirst()
                        .orElse(null));

        Set<String> enabledModules = new HashSet<>();
        String planName = plan != null ? plan.getName() : planCode;
        if (plan != null && plan.getModules() != null) {
            enabledModules.addAll(plan.getModules().stream()
                    .map(PlatformModule::getCode)
                    .collect(Collectors.toSet()));
        }

        // Tier 1: Direct Overrides (Trials and Add-ons)
        List<TenantEntitlementOverride> overrides = overrideRepo.findBySchoolId(schoolId);
        Map<String, String> activeOverrides = new HashMap<>();
        boolean isTrialActive = false;
        LocalDateTime now = LocalDateTime.now();

        for (TenantEntitlementOverride o : overrides) {
            if (o.getExpiresAt() == null || o.getExpiresAt().isAfter(now)) {
                activeOverrides.put(o.getModuleCode(), o.getOverrideType());
                enabledModules.add(o.getModuleCode());
                if ("TRIAL".equalsIgnoreCase(o.getOverrideType()) || "FREE_TRIAL".equalsIgnoreCase(o.getOverrideType())) {
                    isTrialActive = true;
                }
            }
        }

        return new EntitlementEvaluationDto(
                schoolId,
                planCode,
                planName,
                enabledModules,
                activeOverrides,
                isTrialActive,
                now
        );
    }

    @Transactional
    public void grantOverride(Long schoolId, String moduleCode, String overrideType, Integer durationDays) {
        School school = schoolRepo.findById(schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("School not found with ID: " + schoolId));

        List<TenantEntitlementOverride> existing = overrideRepo.findBySchoolIdAndModuleCode(schoolId, moduleCode);
        if (!existing.isEmpty()) {
            overrideRepo.deleteAll(existing);
        }

        TenantEntitlementOverride override = new TenantEntitlementOverride();
        override.setSchool(school);
        override.setModuleCode(moduleCode);
        override.setOverrideType(overrideType != null ? overrideType.toUpperCase() : "ADD_ON");
        if (durationDays != null && durationDays > 0) {
            override.setExpiresAt(LocalDateTime.now().plusDays(durationDays));
        }
        overrideRepo.save(override);
    }

    @Transactional(readOnly = true)
    public boolean hasModuleAccess(Long schoolId, String moduleCode) {
        if (moduleCode == null || moduleCode.trim().isEmpty()) {
            return true;
        }
        // Core mandatory modules are always granted
        if ("ATTENDANCE".equalsIgnoreCase(moduleCode) || "COMMUNICATION".equalsIgnoreCase(moduleCode)) {
            return true;
        }
        EntitlementEvaluationDto eval = evaluateEntitlements(schoolId);
        return eval.getEnabledModules() != null && eval.getEnabledModules().stream()
                .anyMatch(m -> m.equalsIgnoreCase(moduleCode));
    }

    @Transactional(readOnly = true)
    public void enforceModuleAccess(Long schoolId, String moduleCode) {
        if (!hasModuleAccess(schoolId, moduleCode)) {
            throw new com.school.erp.exception.ForbiddenException(
                    "Access Denied (403): Module '" + moduleCode + "' is not enabled under school's active subscription plan or entitlement overrides."
            );
        }
    }
}
