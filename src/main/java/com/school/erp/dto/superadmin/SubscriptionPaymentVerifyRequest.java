package com.school.erp.dto.superadmin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SubscriptionPaymentVerifyRequest(
        @NotNull(message = "schoolId is required")
        Long schoolId,

        @NotBlank(message = "razorpayOrderId is required")
        String razorpayOrderId,

        @NotBlank(message = "razorpayPaymentId is required")
        String razorpayPaymentId,

        @NotBlank(message = "razorpaySignature is required")
        String razorpaySignature,

        String planName,
        String billingCycle,
        Integer studentCount
) {}
