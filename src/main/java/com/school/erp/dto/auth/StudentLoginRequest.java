package com.school.erp.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record StudentLoginRequest(
        @NotBlank(message = "Admission number is required")
        String admissionNo,

        @NotBlank(message = "Password is required")
        String password,

        String schoolCode
) {
}
