package com.school.erp.dto.academicyear;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record AcademicYearRequest(
        @NotBlank(message = "name is required")
        String name,
        
        @NotNull(message = "startDate is required")
        LocalDate startDate,
        
        @NotNull(message = "endDate is required")
        LocalDate endDate,
        
        String status,
        
        Long schoolId
) {
}
