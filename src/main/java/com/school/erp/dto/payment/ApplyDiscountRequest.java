package com.school.erp.dto.payment;

import java.math.BigDecimal;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;

public record ApplyDiscountRequest(
        @NotNull(message = "Discount amount cannot be null")
        @DecimalMin(value = "0.0", inclusive = false, message = "Discount amount must be greater than zero")
        BigDecimal discountAmount
) {
}
