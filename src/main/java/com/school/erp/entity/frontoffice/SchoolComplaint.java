package com.school.erp.entity.frontoffice;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.school.erp.entity.superadmin.School;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "school_complaints")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class SchoolComplaint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "school_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private School school;

    @Column(name = "complainant_name", nullable = false)
    private String complainantName;

    @Column(name = "mobile_number", length = 50)
    private String mobileNumber;

    @Column(name = "complaint_type", nullable = false, length = 100)
    private String complaintType;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "assigned_by", length = 100)
    private String assignedBy;

    @Column(name = "action_taken", columnDefinition = "TEXT")
    private String actionTaken;

    @Column(name = "status", nullable = false, length = 50)
    private String status = "Open";

    @Column(name = "complaint_date")
    private LocalDate complaintDate = LocalDate.now();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
