package com.school.erp.controller;

import com.school.erp.api.ApiResponse;
import com.school.erp.dto.onboarding.*;
import com.school.erp.service.OnboardingDraftService;
import com.school.erp.service.OnboardingService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/onboarding", "/api/v1/onboarding"})
public class OnboardingController {

    private final OnboardingService onboardingService;
    private final OnboardingDraftService draftService;

    public OnboardingController(OnboardingService onboardingService, OnboardingDraftService draftService) {
        this.onboardingService = onboardingService;
        this.draftService = draftService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<OnboardingRegisterResponse>> register(@Valid @RequestBody OnboardingRegisterRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                onboardingService.registerSchool(request),
                "School onboarded successfully"
        ));
    }

    @PostMapping("/init")
    public ResponseEntity<ApiResponse<OnboardingDraftDTO>> initDraft(@Valid @RequestBody OnboardingInitRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                draftService.initDraft(request),
                "Onboarding draft initialized successfully"
        ));
    }

    @GetMapping("/draft/{schoolId}")
    public ResponseEntity<ApiResponse<OnboardingDraftDTO>> getDraft(@PathVariable Long schoolId) {
        return ResponseEntity.ok(ApiResponse.success(
                draftService.getDraft(schoolId),
                "Onboarding draft retrieved successfully"
        ));
    }

    @PostMapping("/step")
    public ResponseEntity<ApiResponse<OnboardingDraftDTO>> saveStep(@Valid @RequestBody OnboardingStepRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                draftService.saveStep(request),
                "Onboarding step saved successfully"
        ));
    }

    @PutMapping("/draft/{schoolId}/step/{stepNumber}")
    public ResponseEntity<ApiResponse<OnboardingDraftDTO>> saveStepPut(@PathVariable Long schoolId,
                                                                       @PathVariable Integer stepNumber,
                                                                       @RequestBody java.util.Map<String, Object> payload) {
        return ResponseEntity.ok(ApiResponse.success(
                draftService.saveStep(new OnboardingStepRequest(schoolId, stepNumber, payload)),
                "Onboarding step saved via PUT successfully"
        ));
    }

    @PostMapping("/activate/{schoolId}")
    public ResponseEntity<ApiResponse<OnboardingDraftDTO>> activateSchool(@PathVariable Long schoolId) {
        return ResponseEntity.ok(ApiResponse.success(
                draftService.activateSchool(schoolId),
                "School activated successfully"
        ));
    }
}
