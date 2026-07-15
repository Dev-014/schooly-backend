package com.school.erp.controller;

import com.school.erp.api.ApiResponse;
import com.school.erp.dto.onboarding.OnboardingRegisterRequest;
import com.school.erp.dto.onboarding.OnboardingRegisterResponse;
import com.school.erp.service.OnboardingService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/onboarding")
public class OnboardingController {

    private final OnboardingService onboardingService;

    public OnboardingController(OnboardingService onboardingService) {
        this.onboardingService = onboardingService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<OnboardingRegisterResponse>> register(@Valid @RequestBody OnboardingRegisterRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                onboardingService.registerSchool(request),
                "School onboarded successfully"
        ));
    }
}
