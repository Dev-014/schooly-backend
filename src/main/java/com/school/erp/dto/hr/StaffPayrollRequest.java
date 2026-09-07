package com.school.erp.dto.hr;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class StaffPayrollRequest {
    @NotNull(message = "Staff ID is required")
    private Long staffId;

    @NotNull(message = "Payroll Month is required")
    private String payrollMonth;

    @NotNull(message = "Payroll Year is required")
    private Integer payrollYear;

    private BigDecimal basicSalary;
    private BigDecimal totalEarnings;
    private BigDecimal totalDeductions;
    private BigDecimal netPayableSalary;
}
