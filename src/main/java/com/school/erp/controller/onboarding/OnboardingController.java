package com.school.erp.controller.onboarding;

import com.school.erp.api.ApiResponse;
import com.school.erp.dto.onboarding.*;
import com.school.erp.service.onboarding.OnboardingDraftService;
import com.school.erp.service.onboarding.OnboardingService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/onboarding", "/api/v1/onboarding"})
public class OnboardingController {

    private final OnboardingService onboardingService;
    private final OnboardingDraftService draftService;
    private final com.school.erp.service.superadmin.SchoolSubscriptionService subscriptionService;

    public OnboardingController(OnboardingService onboardingService,
                                OnboardingDraftService draftService,
                                com.school.erp.service.superadmin.SchoolSubscriptionService subscriptionService) {
        this.onboardingService = onboardingService;
        this.draftService = draftService;
        this.subscriptionService = subscriptionService;
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
    public ResponseEntity<ApiResponse<OnboardingActivationResponse>> activateSchool(@PathVariable Long schoolId) {
        return ResponseEntity.ok(ApiResponse.success(
                draftService.activateSchool(schoolId),
                "School activated successfully"
        ));
    }

    @PostMapping("/subscription/create-order")
    public ResponseEntity<ApiResponse<com.school.erp.dto.payment.GatewayOrderResponse>> createSubscriptionOrder(
            @Valid @RequestBody com.school.erp.dto.superadmin.SubscriptionOrderCreateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                subscriptionService.createSubscriptionOrder(request),
                "Subscription order created successfully"
        ));
    }

    @PostMapping("/subscription/verify")
    public ResponseEntity<ApiResponse<com.school.erp.dto.payment.GatewayVerificationResponse>> verifySubscriptionPayment(
            @Valid @RequestBody com.school.erp.dto.superadmin.SubscriptionPaymentVerifyRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                subscriptionService.verifySubscriptionPayment(request),
                "Subscription payment verified successfully"
        ));
    }
}
