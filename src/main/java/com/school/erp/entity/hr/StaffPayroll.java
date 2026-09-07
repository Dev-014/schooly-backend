package com.school.erp.entity.hr;

import com.school.erp.entity.School;
import com.school.erp.entity.Staff;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "staff_payrolls")
public class StaffPayroll {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "staff_id", nullable = false)
    private Staff staff;

    @Column(name = "payroll_month", nullable = false)
    private String payrollMonth;

    @Column(name = "payroll_year", nullable = false)
    private Integer payrollYear;

    @Column(name = "basic_salary", nullable = false)
    private BigDecimal basicSalary;

    @Column(name = "total_earnings", nullable = false)
    private BigDecimal totalEarnings;

    @Column(name = "total_deductions", nullable = false)
    private BigDecimal totalDeductions;

    @Column(name = "net_payable_salary", nullable = false)
    private BigDecimal netPayableSalary;

    @Column(name = "status")
    private String status = "PENDING";

    @Column(name = "payment_date")
    private LocalDate paymentDate;

    @Column(name = "details_json", columnDefinition = "JSONB")
    private String detailsJson;
}
