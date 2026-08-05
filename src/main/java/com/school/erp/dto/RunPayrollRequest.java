package com.school.erp.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class RunPayrollRequest {
    @NotNull(message = "Employee ID is required")
    private Long employeeId;
    
    @NotBlank(message = "Month is required")
    private String month;
    
    @NotNull(message = "Year is required")
    private Integer year;
    
    @NotNull(message = "Base salary is required")
    @Min(value = 0, message = "Base salary must be non-negative")
    private BigDecimal baseSalary;
    
    private BigDecimal allowances = BigDecimal.ZERO;
    private BigDecimal deductions = BigDecimal.ZERO;
    
    private String status = "PENDING";
    private LocalDate paymentDate;
}
