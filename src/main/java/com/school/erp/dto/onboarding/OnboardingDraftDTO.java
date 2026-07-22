package com.school.erp.dto.onboarding;

import java.util.Map;

public record OnboardingDraftDTO(
        Long schoolId,
        String status,
        Integer currentStep,
        Map<String, Object> step1,
        Map<String, Object> step2,
        Map<String, Object> step3,
        Map<String, Object> step4,
        Map<String, Object> step5,
        Map<String, Object> step6,
        Map<String, Object> step7,
        Map<String, Object> step8,
        Map<String, Object> step9,
        Map<String, Object> step10,
        Map<String, Object> step11,
        String updatedAt
) {
}
