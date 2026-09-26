package com.school.erp.dto.hr;

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
public class StaffPayslipDTO {
    private Long payrollId;
    private Long schoolId;
    private String schoolName;
    private Long staffId;
    private String staffName;
    private String staffCode;
    private String department;
    private String designation;
    private String bankAccount;
    private String bankName;
    private String ifscCode;
    private String payrollMonth;
    private Integer payrollYear;
    
    // Earnings
    private BigDecimal basicSalary;
    private BigDecimal hra;
    private BigDecimal da;
    private BigDecimal ta;
    private BigDecimal specialAllowance;
    private BigDecimal totalEarnings;

    // Deductions
    private BigDecimal pf;
    private String epfNumber;
    private BigDecimal tds;
    private BigDecimal esic;
    private BigDecimal otherDeductions;
    private BigDecimal totalDeductions;

    // Net
    private BigDecimal netPayableSalary;
    private String status;
    private LocalDate paymentDate;
}
