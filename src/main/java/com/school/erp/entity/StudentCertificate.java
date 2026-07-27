package com.school.erp.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "student_certificates")
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
public class StudentCertificate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(name = "certificate_type", nullable = false)
    private String certificateType; // e.g. BONAFIDE, CHARACTER, TRANSFER

    @Column(name = "issue_date")
    private LocalDate issueDate;

    @Column(name = "status", nullable = false)
    private String status = "PENDING"; // PENDING, ISSUED, REJECTED

    @Column(name = "remarks")
    private String remarks;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
