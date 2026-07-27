package com.school.erp.dto.academic;

import java.time.LocalDate;

public record AcademicYearResponse(
        Long id,
        Long schoolId,
        String name,
        LocalDate startDate,
        LocalDate endDate,
        String status
) {}
