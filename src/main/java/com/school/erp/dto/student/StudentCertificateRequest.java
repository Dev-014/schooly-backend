package com.school.erp.dto.student;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record StudentCertificateRequest(
        @NotNull(message = "School ID is required") Long schoolId,
        @NotNull(message = "Student ID is required") Long studentId,
        @NotBlank(message = "Certificate Type is required") String certificateType,
        LocalDate issueDate,
        String status,
        String remarks
) {}
