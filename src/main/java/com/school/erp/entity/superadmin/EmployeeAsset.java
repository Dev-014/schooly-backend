package com.school.erp.entity.superadmin;

import com.school.erp.entity.SuperAdminEmployee;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "employee_assets")
@Data
public class EmployeeAsset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private SuperAdminEmployee employee;

    @Column(nullable = false, length = 100)
    private String assetName;

    @Column(nullable = false, length = 50)
    private String assetType;

    @Column(length = 100)
    private String serialNumber;

    @Column(nullable = false)
    private LocalDateTime assignedDate = LocalDateTime.now();

    private LocalDateTime returnDate;

    @Column(nullable = false, length = 50)
    private String status = "ASSIGNED";

    @Column(columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
