package com.school.erp.dto.onboarding;

public record AdminCredentialsDTO(
        String username,
        String phone,
        String rawPassword
) {
}
