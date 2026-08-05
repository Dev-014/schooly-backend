package com.school.erp.entity.superadmin;

import com.school.erp.entity.SuperAdminEmployee;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "employee_audit_logs")
@Data
public class EmployeeAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    private SuperAdminEmployee employee;
    
    @Column(nullable = false, length = 100)
    private String action;
    
    @Column(nullable = false, length = 100)
    private String entityType;
    
    @Column(nullable = false)
    private Long entityId;
    
    @Column(columnDefinition = "TEXT")
    private String details;
    
    @Column(updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
