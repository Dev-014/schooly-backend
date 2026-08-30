package com.school.erp.dto.payment;

public record FeeGenerationConfirmRequest(
        Long classId,
        Long academicYearId,
        Long feeStructureId
) {}
