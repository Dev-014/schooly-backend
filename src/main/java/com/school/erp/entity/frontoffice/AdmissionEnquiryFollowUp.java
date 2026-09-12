package com.school.erp.entity.frontoffice;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.school.erp.entity.Staff;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "admission_enquiry_follow_ups")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class AdmissionEnquiryFollowUp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "enquiry_id", nullable = false)
    @JsonIgnoreProperties({"followUps", "hibernateLazyInitializer", "handler"})
    private AdmissionEnquiry enquiry;

    @Column(name = "action_type", nullable = false, length = 100)
    private String actionType;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "follow_up_date", nullable = false)
    private LocalDateTime followUpDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recorded_by")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Staff recordedBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
