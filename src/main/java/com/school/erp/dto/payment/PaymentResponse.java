package com.school.erp.dto.payment;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

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
        List<PaymentAllocationResponse> allocations,
        String verificationStatus,
        String verificationNote,
        UUID receiptFileId
) {
    // Backwards-compatible constructor for existing 12-parameter calls
    public PaymentResponse(
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
        this(id, schoolId, amount, paymentMode, transactionId, status, receiptNumber,
             studentName, className, paymentDate, createdAt, allocations, "APPROVED", null, null);
    }

    public record PaymentAllocationResponse(
            Long feeDueId,
            String feeTitle,
            BigDecimal allocatedAmount
    ) {}
}
