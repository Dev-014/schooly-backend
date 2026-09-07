package com.school.erp.entity.hr;

import com.school.erp.entity.School;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "recruitment_candidates")
public class RecruitmentCandidate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "phone")
    private String phone;

    @Column(name = "email")
    private String email;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "applying_for")
    private String applyingFor;

    @Column(name = "expected_salary")
    private BigDecimal expectedSalary;

    @Column(name = "marital_status")
    private String maritalStatus;

    @Column(name = "work_experience", columnDefinition = "TEXT")
    private String workExperience;

    @Column(name = "interview_date")
    private LocalDateTime interviewDate;

    @Column(name = "submission_date")
    private LocalDateTime submissionDate = LocalDateTime.now();

    @Column(name = "status")
    private String status = "NEW_APPLICATION";

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "document_url")
    private String documentUrl;
}
