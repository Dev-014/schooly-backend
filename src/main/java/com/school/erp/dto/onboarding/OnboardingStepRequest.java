package com.school.erp.dto.onboarding;

import jakarta.validation.constraints.NotNull;
import java.util.Map;

public record OnboardingStepRequest(
        @NotNull(message = "School ID is required")
        Long schoolId,
        @NotNull(message = "Step number is required")
        Integer stepNumber,
        @NotNull(message = "Payload map is required")
        Map<String, Object> payload
) {
}
