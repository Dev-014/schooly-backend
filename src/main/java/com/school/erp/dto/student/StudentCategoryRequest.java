package com.school.erp.dto.student;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record StudentCategoryRequest(
        @NotNull(message = "schoolId is required") Long schoolId,
        @NotBlank(message = "name is required") String name,
        String description
) {
}
