package com.school.erp.dto.onboarding;

public record OnboardingActivationResponse(
        OnboardingDraftDTO draft,
        AdminCredentialsDTO adminCredentials
) {
}
