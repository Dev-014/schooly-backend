package com.school.erp.dto.student;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record StudentPromotionRequest(
        @NotEmpty(message = "Student IDs cannot be empty")
        List<Long> studentIds,

        @NotNull(message = "Target class ID is required")
        Long targetClassId,

        Long targetSectionId,
        Long targetAcademicYearId
) {}
