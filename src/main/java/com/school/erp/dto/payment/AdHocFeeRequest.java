package com.school.erp.dto.payment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;

public record AdHocFeeRequest(
        @NotNull(message = "Fee category is required") Long feeCategoryId,
        @NotNull(message = "Amount is required") @Positive(message = "Amount must be positive") BigDecimal amount,
        @NotBlank(message = "Title is required") String title,
        @NotNull(message = "Due date is required") LocalDate dueDate
) {}
