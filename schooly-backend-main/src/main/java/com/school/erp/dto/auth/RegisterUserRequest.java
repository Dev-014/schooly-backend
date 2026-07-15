package com.school.erp.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RegisterUserRequest(
        @NotBlank(message = "phone is required")
        String phone,
        String name,
        String email,
        String password,
        @NotNull(message = "schoolId is required")
        Long schoolId,
        @NotBlank(message = "role is required")
        String role
) {
}
