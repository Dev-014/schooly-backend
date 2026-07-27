package com.school.erp.dto.academic;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SectionRequest(
        Long schoolId,
        @NotNull(message = "Class ID is required")
        Long classId,
        @NotBlank(message = "Section name is required")
        String name,
        String roomNumber,
        Integer capacity
) {}
