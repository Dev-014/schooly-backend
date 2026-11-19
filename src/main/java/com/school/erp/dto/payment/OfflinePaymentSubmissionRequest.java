package com.school.erp.dto.payment;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record OfflinePaymentSubmissionRequest(
        @NotNull(message = "studentId is required")
        Long studentId,

        Long schoolId,

        @NotNull(message = "feeDueIds is required")
        List<Long> feeDueIds,

        @NotNull(message = "totalAmount is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "totalAmount must be greater than zero")
        BigDecimal totalAmount,

        @NotBlank(message = "paymentMode is required")
        String paymentMode, // CASH, CHEQUE, BANK_TRANSFER, DD

        LocalDate paymentDate,

        String referenceNumber, // Cheque number, UTR number, Receipt book slip no

        UUID receiptFileId, // Centralized file ID uploaded via /api/v1/files/upload

        String note
) {}
