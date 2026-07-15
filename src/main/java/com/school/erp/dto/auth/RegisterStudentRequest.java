package com.school.erp.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record RegisterStudentRequest(
        @NotBlank(message = "phone is required")
        String phone,
        String name,
        String email,
        @NotNull(message = "schoolId is required")
        Long schoolId,
        @NotNull(message = "classId is required")
        Long classId,
        Long sectionId,
        Long academicYearId,
        String admissionNo,
        String rollNumber,
        LocalDate admissionDate
) {
}
