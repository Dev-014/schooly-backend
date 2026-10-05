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
@Table(name = "school_entrance_exams")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class SchoolEntranceExam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "school_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private School school;

    @Column(name = "candidate_name", nullable = false)
    private String candidateName;

    @Column(name = "mobile_number", length = 50)
    private String mobileNumber;

    @Column(name = "parent_name")
    private String parentName;

    @Column(name = "gender", length = 20)
    private String gender;

    @Column(name = "class_name", length = 100)
    private String className;

    @Column(name = "exam_name", length = 150)
    private String examName;

    @Column(name = "center_name", length = 150)
    private String centerName;

    @Column(name = "exam_date")
    private LocalDate examDate;

    @Column(name = "exam_time", length = 50)
    private String examTime;

    @Column(name = "status", nullable = false, length = 50)
    private String status = "Scheduled";

    @Column(name = "score")
    private Double score;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
