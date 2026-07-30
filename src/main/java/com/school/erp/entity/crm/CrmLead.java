package com.school.erp.entity.crm;

import com.school.erp.entity.School;
import com.school.erp.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "crm_leads")
public class CrmLead {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "school_name", nullable = false)
    private String schoolName;

    @Column(name = "principal_name")
    private String principalName;

    private String city;
    private String board;

    @Column(nullable = false)
    private String mobile;

    @Column(name = "alternative_mobile")
    private String alternativeMobile;

    private String email;
    private String address;
    private String state;

    @Column(name = "pin_code")
    private String pinCode;

    @Column(name = "approx_student_strength")
    private Integer approxStudentStrength;

    private Integer teachers;
    private Integer branches;

    @Column(name = "current_erp")
    private String currentErp;

    private String website;

    @Column(name = "existing_problems")
    private String existingProblems;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "lead_source")
    private CrmLeadSource leadSource = CrmLeadSource.OTHER;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_employee_id")
    private User assignedEmployee;

    private String priority = "MEDIUM";

    @Column(name = "expected_closing_date")
    private LocalDateTime expectedClosingDate;

    @Column(name = "lead_rating")
    private Integer leadRating = 1;

    private String notes;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "pipeline_stage")
    private CrmPipelineStage pipelineStage = CrmPipelineStage.NEW;

    private String status = "ACTIVE";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "converted_school_id")
    private School convertedSchool;

    @Column(name = "lost_reason")
    private String lostReason;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @OneToMany(mappedBy = "lead", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CrmFollowUp> followUps = new ArrayList<>();

    @OneToMany(mappedBy = "lead", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CrmDemo> demos = new ArrayList<>();

    @OneToMany(mappedBy = "lead", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CrmQuotation> quotations = new ArrayList<>();

    @OneToMany(mappedBy = "lead", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CrmActivityLog> activityLogs = new ArrayList<>();

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
