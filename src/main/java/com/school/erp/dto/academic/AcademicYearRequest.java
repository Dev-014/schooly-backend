package com.school.erp.dto.academic;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

public record AcademicYearRequest(
        Long schoolId,
        @NotBlank(message = "Academic year name is required")
        String name,
        LocalDate startDate,
        LocalDate endDate,
        String status
) {}
