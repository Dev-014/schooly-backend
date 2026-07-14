package com.school.erp.dto.schoolclass;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SchoolClassRequest(
        @NotBlank(message = "name is required")
        String name,
        Long schoolId
) {
}
