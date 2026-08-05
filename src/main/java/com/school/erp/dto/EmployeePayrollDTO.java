package com.school.erp.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class EmployeePayrollDTO {
    private Long id;
    private Long employeeId;
    private String employeeName;
    private String month;
    private Integer year;
    private BigDecimal baseSalary;
    private BigDecimal allowances;
    private BigDecimal deductions;
    private BigDecimal netSalary;
    private String status;
    private LocalDate paymentDate;
}
