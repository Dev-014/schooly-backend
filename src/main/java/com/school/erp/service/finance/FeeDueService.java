package com.school.erp.service;

import com.school.erp.dto.payment.FeeDueResponse;
import com.school.erp.dto.payment.ApplyDiscountRequest;

import java.util.List;

public interface FeeDueService {
    List<FeeDueResponse> getStudentDues(Long studentId, Long schoolId, List<String> statuses);
    FeeDueResponse applyDiscount(Long studentId, Long dueId, Long schoolId, ApplyDiscountRequest request);
    void generateDuesFromStructure(Long studentId, Long feeStructureId, Long academicYearId, Long schoolId);
    List<FeeDueResponse> generateBaseDuesForStudent(Long studentId, Long schoolId);
    FeeDueResponse assignAdHocFee(Long studentId, Long schoolId, Long feeCategoryId, java.math.BigDecimal amount, String title, java.time.LocalDate dueDate);
    com.school.erp.dto.payment.FeeGenerationPreviewResponse previewBatchGeneration(Long schoolId, com.school.erp.dto.payment.FeeGenerationPreviewRequest request);
    void confirmBatchGeneration(Long schoolId, com.school.erp.dto.payment.FeeGenerationConfirmRequest request, String generatedBy);
}
