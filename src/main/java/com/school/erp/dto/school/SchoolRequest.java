package com.school.erp.dto.school;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record SchoolRequest(
        @NotBlank(message = "name is required")
        String name,
        @NotBlank(message = "code is required")
        String code,
        @Email(message = "contactEmail must be valid")
        String contactEmail,
        String contactPhone,
        String address,
        @NotBlank(message = "status is required")
        String status
) {
}
