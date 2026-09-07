package com.school.erp.entity.hr;

import com.school.erp.entity.Staff;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "staff_payroll_details")
public class StaffPayrollDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "staff_id", nullable = false, unique = true)
    private Staff staff;

    @Column(name = "basic_salary")
    private BigDecimal basicSalary;

    @Column(name = "hra")
    private BigDecimal hra;

    @Column(name = "ta")
    private BigDecimal ta;

    @Column(name = "da")
    private BigDecimal da;

    @Column(name = "special_allowance")
    private BigDecimal specialAllowance;

    @Column(name = "pf")
    private BigDecimal pf;

    @Column(name = "epf_number")
    private String epfNumber;

    @Column(name = "tds")
    private BigDecimal tds;

    @Column(name = "esic")
    private BigDecimal esic;

    @Column(name = "other_deductions")
    private BigDecimal otherDeductions;
}
