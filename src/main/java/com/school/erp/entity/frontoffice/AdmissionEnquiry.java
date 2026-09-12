package com.school.erp.entity.frontoffice;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.school.erp.entity.School;
import com.school.erp.entity.SchoolClass;
import com.school.erp.entity.Staff;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "admission_enquiries")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class AdmissionEnquiry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "school_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private School school;

    @Column(name = "enquiry_number", length = 50)
    private String enquiryNumber;

    @Column(name = "student_name", nullable = false)
    private String studentName;

    @Column(name = "parent_name")
    private String parentName;

    @Column(name = "phone", length = 50)
    private String phone;

    @Column(name = "email")
    private String email;

    @Column(name = "enquiry_date", nullable = false)
    private LocalDate enquiryDate;

    @Column(name = "next_follow_up_date")
    private LocalDate nextFollowUpDate;

    @Column(name = "source", length = 100)
    private String source;

    @Column(name = "status", length = 50)
    private String status = "ACTIVE";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private SchoolClass schoolClass;

    @Column(name = "number_of_children")
    private Integer numberOfChildren = 1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Staff assignedTo;

    @Column(name = "detailed_notes", columnDefinition = "TEXT")
    private String detailedNotes;

    @OneToMany(mappedBy = "enquiry", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("followUpDate DESC")
    @JsonIgnoreProperties({"enquiry", "hibernateLazyInitializer", "handler"})
    private List<AdmissionEnquiryFollowUp> followUps = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
