package com.school.erp.dto.payment;

import java.math.BigDecimal;

public record GatewayOrderResponse(
        String orderId,
        Long internalPaymentId,
        BigDecimal amount,
        Long amountInPaise,
        String currency,
        String keyId,
        String schoolName,
        String studentName,
        String receiptNumber,
        String accountId,
        boolean isSandbox
) {}
