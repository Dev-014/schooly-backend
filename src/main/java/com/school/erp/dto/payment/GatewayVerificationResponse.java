package com.school.erp.dto.payment;

import java.math.BigDecimal;

public record GatewayVerificationResponse(
        boolean verified,
        Long paymentId,
        String receiptNumber,
        String status,
        BigDecimal amount,
        String message
) {}
