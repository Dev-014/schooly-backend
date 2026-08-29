package com.school.erp.dto.payment;

import java.math.BigDecimal;
import java.time.LocalDate;

public record FeeDueResponse(
        Long id,
        String title,
        BigDecimal amount,
        BigDecimal paidAmount,
        BigDecimal discountAmount,
        LocalDate dueDate,
        String status,
        Long feeCategoryId,
        String feeCategoryName,
        String termName
) {
}
