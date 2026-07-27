package com.school.erp.dto.academic;

import jakarta.validation.constraints.NotBlank;

public record SubjectRequest(
        Long schoolId,
        @NotBlank(message = "Subject name is required")
        String name,
        String type,
        Integer credits,
        String gradeLevel
) {}
