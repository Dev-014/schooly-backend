package com.school.erp.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "student_referrals")
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
public class StudentReferral {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

    @Column(name = "referral_by", nullable = false)
    private String referralBy;

    @Column(name = "student_name", nullable = false)
    private String studentName;

    @Column(name = "email")
    private String email;

    @Column(name = "mobile", nullable = false)
    private String mobile;

    @Column(name = "note")
    private String note;

    @Column(name = "status", nullable = false)
    private String status = "PENDING";

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
