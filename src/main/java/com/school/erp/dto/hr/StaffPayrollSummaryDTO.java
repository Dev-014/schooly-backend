package com.school.erp.dto.hr;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffPayrollSummaryDTO {
    private Long schoolId;
    private String payrollMonth;
    private Integer payrollYear;
    private BigDecimal totalPayout;
    private BigDecimal pendingSalaries;
    private BigDecimal totalDeductions;
    private Long totalStaff;
    private Long staffPaidCount;
    private Long staffPendingCount;
    private Double paidPercentage;
}
