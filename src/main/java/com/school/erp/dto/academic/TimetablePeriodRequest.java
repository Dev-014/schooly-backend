package com.school.erp.dto.academic;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TimetablePeriodRequest(
        Long schoolId,
        @NotNull(message = "Period number is required")
        Integer periodNumber,
        @NotBlank(message = "Period name is required")
        String name,
        String startTime,
        String endTime,
        Boolean isBreak
) {}
