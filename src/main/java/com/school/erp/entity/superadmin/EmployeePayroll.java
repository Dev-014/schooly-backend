package com.school.erp.entity.superadmin;

import com.school.erp.entity.SuperAdminEmployee;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "employee_payroll")
@Data
public class EmployeePayroll {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private SuperAdminEmployee employee;
    
    @Column(nullable = false, length = 20)
    private String month;
    
    @Column(nullable = false)
    private Integer year;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal baseSalary;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal allowances = BigDecimal.ZERO;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal deductions = BigDecimal.ZERO;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal netSalary;
    
    @Column(nullable = false, length = 50)
    private String status = "PENDING";
    
    private LocalDate paymentDate;
    
    @Column(updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
