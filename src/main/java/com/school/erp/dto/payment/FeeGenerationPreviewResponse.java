package com.school.erp.dto.payment;

import java.math.BigDecimal;
import java.util.List;

public record FeeGenerationPreviewResponse(
        int eligibleStudents,
        int totalStudentsInClass,
        BigDecimal totalAmountGenerated,
        List<FeeGenerationPreviewStudent> studentsToGenerate
) {
    public record FeeGenerationPreviewStudent(
            Long studentId,
            String admissionNo,
            String studentName,
            BigDecimal amount
    ) {}
}
