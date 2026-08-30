package com.school.erp.dto.payment;

public record FeeGenerationPreviewRequest(
        Long classId,
        Long academicYearId,
        Long feeStructureId
) {}
