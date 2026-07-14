package com.school.erp.dto.feeinvoice;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record FeeInvoiceResponse(
        Long id,
        Long studentId,
        Long schoolId,
        Long academicYearId,
        LocalDate dueDate,
        BigDecimal totalAmount,
        BigDecimal paidAmount,
        String status,
        LocalDateTime createdAt
) {
}
