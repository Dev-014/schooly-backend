package com.school.erp.dto.payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentResponse(
        Long id,
        Long invoiceId,
        Long schoolId,
        BigDecimal amount,
        String paymentMode,
        String transactionId,
        String status,
        LocalDateTime createdAt
) {
}
