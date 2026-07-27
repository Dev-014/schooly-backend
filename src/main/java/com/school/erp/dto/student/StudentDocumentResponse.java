package com.school.erp.dto.student;

import java.time.LocalDateTime;

public record StudentDocumentResponse(
        Long id,
        Long studentId,
        Long schoolId,
        String documentName,
        String documentType,
        String fileUrl,
        LocalDateTime uploadedAt
) {}
