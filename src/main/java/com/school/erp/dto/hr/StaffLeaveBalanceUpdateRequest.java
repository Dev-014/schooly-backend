package com.school.erp.dto.hr;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffLeaveBalanceUpdateRequest {
    @NotNull(message = "Leave type ID is required")
    private Long leaveTypeId;

    @NotNull(message = "Total leaves allocation is required")
    private BigDecimal totalLeaves;

    private BigDecimal usedLeaves;

    private BigDecimal remainingLeaves;
}
