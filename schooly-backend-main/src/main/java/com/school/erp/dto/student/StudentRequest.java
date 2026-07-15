package com.school.erp.dto.student;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record StudentRequest(
        @NotNull(message = "userId is required")
        Long userId,
        String name,
        @NotBlank(message = "admissionNo is required")
        String admissionNo,
        String rollNumber,
        @NotBlank(message = "status is required")
        String status,
        @NotNull(message = "admissionDate is required")
        LocalDate admissionDate,
        Long schoolId,
        @NotNull(message = "classId is required")
        Long classId,
        @NotNull(message = "sectionId is required")
        Long sectionId,
        @NotNull(message = "academicYearId is required")
        Long academicYearId
) {
}
