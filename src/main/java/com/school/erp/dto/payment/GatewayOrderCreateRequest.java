package com.school.erp.dto.payment;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

public record GatewayOrderCreateRequest(
        @NotNull(message = "studentId is required")
        Long studentId,

        Long schoolId,

        @NotNull(message = "feeDueIds is required")
        List<Long> feeDueIds,

        @NotNull(message = "totalAmount is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "totalAmount must be greater than zero")
        BigDecimal totalAmount,

        String currency
) {}
