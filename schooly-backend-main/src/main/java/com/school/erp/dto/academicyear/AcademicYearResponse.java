package com.school.erp.dto.academicyear;

import java.time.LocalDate;

public record AcademicYearResponse(
        Long id,
        String name,
        LocalDate startDate,
        LocalDate endDate,
        String status,
        Long schoolId
) {
}
