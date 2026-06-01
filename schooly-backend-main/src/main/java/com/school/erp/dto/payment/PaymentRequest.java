package com.school.erp.dto.payment;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record PaymentRequest(
        @NotNull(message = "invoiceId is required")
        Long invoiceId,
        Long schoolId,
        @NotNull(message = "amount is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "amount must be greater than zero")
        BigDecimal amount,
        @NotBlank(message = "paymentMode is required")
        String paymentMode,
        String transactionId,
        @NotBlank(message = "status is required")
        String status
) {
}
