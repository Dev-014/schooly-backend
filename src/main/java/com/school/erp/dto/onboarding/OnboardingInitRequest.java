package com.school.erp.dto.onboarding;

import jakarta.validation.constraints.NotBlank;
import java.util.Map;

public record OnboardingInitRequest(
        String schoolName,
        String schoolCode,
        String boardType,
        String principalEmail,
        String adminPhone,
        Map<String, Object> initialMetadata
) {
}
