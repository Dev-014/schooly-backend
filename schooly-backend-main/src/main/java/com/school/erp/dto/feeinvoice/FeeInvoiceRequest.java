package com.school.erp.dto.feeinvoice;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record FeeInvoiceRequest(
        @NotNull(message = "studentId is required")
        Long studentId,
        Long schoolId,
        @NotNull(message = "academicYearId is required")
        Long academicYearId,
        @NotNull(message = "dueDate is required")
        LocalDate dueDate,
        @NotNull(message = "totalAmount is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "totalAmount must be greater than zero")
        BigDecimal totalAmount,
        @NotNull(message = "paidAmount is required")
        @DecimalMin(value = "0.0", message = "paidAmount cannot be negative")
        BigDecimal paidAmount,
        @NotBlank(message = "status is required")
        String status
) {
}
