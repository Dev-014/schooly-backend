package com.school.erp.dto.payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;

public record PaymentResponse(
        Long id,
        Long schoolId,
        BigDecimal amount,
        String paymentMode,
        String transactionId,
        String status,
        String receiptNumber,
        String studentName,
        String className,
        LocalDate paymentDate,
        LocalDateTime createdAt,
        List<PaymentAllocationResponse> allocations
) {
    public record PaymentAllocationResponse(
            Long feeDueId,
            String feeTitle,
            BigDecimal allocatedAmount
    ) {}
}
