package com.school.erp.dto.section;

import jakarta.validation.constraints.NotBlank;

public record SectionRequest(
        @NotBlank(message = "name is required")
        String name,
        Long schoolId
) {
}
