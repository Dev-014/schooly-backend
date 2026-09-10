package com.school.erp.entity.frontoffice;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.school.erp.entity.School;
import com.school.erp.entity.Staff;
import com.school.erp.entity.Student;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "gate_passes")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class GatePass {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "school_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private School school;

    @Column(name = "pass_number", length = 50)
    private String passNumber;

    @Column(name = "person_name", nullable = false)
    private String personName;

    @Column(name = "role", nullable = false, length = 50)
    private String role; // STUDENT, STAFF, VISITOR

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Staff staff;

    @Column(name = "class_or_department", length = 100)
    private String classOrDepartment;

    @Column(name = "reason_for_exit", nullable = false, columnDefinition = "TEXT")
    private String reasonForExit;

    @Column(name = "pass_date", nullable = false)
    private LocalDate passDate;

    @Column(name = "exit_time", nullable = false)
    private LocalTime exitTime;

    @Column(name = "expected_return_time")
    private LocalTime expectedReturnTime;

    @Column(name = "approved_by", nullable = false)
    private String approvedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by_staff_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Staff approvedByStaff;

    @Column(name = "status", length = 50)
    private String status = "APPROVED"; // PENDING, APPROVED, REJECTED, USED, EXPIRED

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
