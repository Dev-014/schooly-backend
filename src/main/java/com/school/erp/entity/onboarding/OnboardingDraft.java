package com.school.erp.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "onboarding_drafts")
public class OnboardingDraft {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "school_id")
    private Long schoolId;

    @Column(name = "status", nullable = false)
    private String status = "DRAFT";

    @Column(name = "current_step", nullable = false)
    private Integer currentStep = 1;

    @Column(name = "step1_data", columnDefinition = "TEXT")
    private String step1Data;

    @Column(name = "step2_data", columnDefinition = "TEXT")
    private String step2Data;

    @Column(name = "step3_data", columnDefinition = "TEXT")
    private String step3Data;

    @Column(name = "step4_data", columnDefinition = "TEXT")
    private String step4Data;

    @Column(name = "step5_data", columnDefinition = "TEXT")
    private String step5Data;

    @Column(name = "step6_data", columnDefinition = "TEXT")
    private String step6Data;

    @Column(name = "step7_data", columnDefinition = "TEXT")
    private String step7Data;

    @Column(name = "step8_data", columnDefinition = "TEXT")
    private String step8Data;

    @Column(name = "step9_data", columnDefinition = "TEXT")
    private String step9Data;

    @Column(name = "step10_data", columnDefinition = "TEXT")
    private String step10Data;

    @Column(name = "step11_data", columnDefinition = "TEXT")
    private String step11Data;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
