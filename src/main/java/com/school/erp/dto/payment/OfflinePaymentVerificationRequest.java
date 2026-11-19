package com.school.erp.dto.payment;

import jakarta.validation.constraints.NotNull;

public record OfflinePaymentVerificationRequest(
        @NotNull(message = "approved status is required")
        Boolean approved,

        String verificationNote
) {}
