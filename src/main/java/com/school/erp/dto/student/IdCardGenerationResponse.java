package com.school.erp.dto.student;

import java.time.LocalDateTime;

public record IdCardGenerationResponse(
        Long id,
        Long schoolId,
        Long studentId,
        String studentName,
        String admissionNo,
        String classKey,
        String status,
        LocalDateTime generatedAt
) {}
