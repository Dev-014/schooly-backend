package com.school.erp.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record AuthLoginRequest(
        @NotBlank(message = "phone is required")
        String phone
) {
}
