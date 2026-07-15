package com.school.erp.dto.onboarding;

import java.util.Map;

public record OnboardingRegisterResponse(
        Long schoolId,
        String schoolName,
        String schoolCode,
        Long adminUserId,
        String adminPhone,
        String status,
        Map<String, Object> metadata
) {
}
