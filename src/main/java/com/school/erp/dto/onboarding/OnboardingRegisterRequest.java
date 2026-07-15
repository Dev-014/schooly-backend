package com.school.erp.dto.onboarding;

import jakarta.validation.constraints.NotBlank;
import java.util.Map;

public record OnboardingRegisterRequest(
        @NotBlank(message = "School name is required")
        String schoolName,

        @NotBlank(message = "School code is required")
        String schoolCode,

        String contactEmail,
        String contactPhone,
        String address,

        @NotBlank(message = "Admin name is required")
        String adminName,

        @NotBlank(message = "Admin phone is required")
        String adminPhone,

        String adminEmail,
        String adminPassword,

        Map<String, Object> metadata
) {
}
