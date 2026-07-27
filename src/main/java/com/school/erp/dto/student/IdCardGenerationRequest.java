package com.school.erp.dto.student;

import jakarta.validation.constraints.NotNull;

public record IdCardGenerationRequest(
        @NotNull(message = "School ID is required") Long schoolId,
        @NotNull(message = "Student ID is required") Long studentId,
        String status
) {}
