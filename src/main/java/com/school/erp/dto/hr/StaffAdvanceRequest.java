package com.school.erp.dto.hr;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffAdvanceRequest {
    @NotNull(message = "Staff ID is required")
    private Long staffId;

    @NotNull(message = "Advance date is required")
    private LocalDate advanceDate;

    @NotNull(message = "Amount is required")
    private BigDecimal amount;

    private String reason;
    private String repaymentMethod;
}
