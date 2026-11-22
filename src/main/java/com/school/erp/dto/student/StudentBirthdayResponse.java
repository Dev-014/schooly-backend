package com.school.erp.dto.student;

import java.time.LocalDate;

public record StudentBirthdayResponse(
        Long id,
        String name,
        String className,
        String sectionName,
        String rollNumber,
        String photoUrl,
        LocalDate dateOfBirth,
        boolean isSelf
) {
}
