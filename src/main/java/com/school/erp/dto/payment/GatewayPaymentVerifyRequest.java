package com.school.erp.dto.payment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record GatewayPaymentVerifyRequest(
        @NotBlank(message = "razorpayOrderId is required")
        String razorpayOrderId,

        @NotBlank(message = "razorpayPaymentId is required")
        String razorpayPaymentId,

        @NotBlank(message = "razorpaySignature is required")
        String razorpaySignature,

        @NotNull(message = "internalPaymentId is required")
        Long internalPaymentId,

        Long schoolId
) {}
