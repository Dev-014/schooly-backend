package com.school.erp.dto.hr;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffPayrollBatchRequest {
    @NotBlank(message = "Payroll month is required")
    private String payrollMonth;

    @NotNull(message = "Payroll year is required")
    private Integer payrollYear;
}
