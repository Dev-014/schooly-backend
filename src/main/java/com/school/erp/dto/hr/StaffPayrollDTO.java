package com.school.erp.dto.hr;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class StaffPayrollDTO {
    private Long id;
    private Long schoolId;
    private Long staffId;
    private String staffName;
    private String payrollMonth;
    private Integer payrollYear;
    private BigDecimal basicSalary;
    private BigDecimal totalEarnings;
    private BigDecimal totalDeductions;
    private BigDecimal netPayableSalary;
    private String status;
    private LocalDate paymentDate;
}
