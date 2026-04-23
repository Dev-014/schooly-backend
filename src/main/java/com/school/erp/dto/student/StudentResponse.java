package com.school.erp.dto.student;

import java.time.LocalDate;

public record StudentResponse(
        Long id,
        Long userId,
        String name,
        String admissionNo,
        String rollNumber,
        String status,
        LocalDate admissionDate,
        Long schoolId,
        Long classId,
        Long sectionId,
        Long academicYearId
) {
}
