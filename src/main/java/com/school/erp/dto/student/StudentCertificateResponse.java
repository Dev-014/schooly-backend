package com.school.erp.dto.student;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record StudentCertificateResponse(
        Long id,
        Long schoolId,
        Long studentId,
        String studentName,
        String admissionNo,
        String certificateType,
        LocalDate issueDate,
        String status,
        String remarks,
        LocalDateTime createdAt
) {}
