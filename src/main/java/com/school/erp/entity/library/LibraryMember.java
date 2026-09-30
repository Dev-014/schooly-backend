package com.school.erp.entity.library;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.school.erp.entity.hr.Staff;
import com.school.erp.entity.student.Student;
import com.school.erp.entity.superadmin.School;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "library_members")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class LibraryMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "school_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private School school;

    @Column(name = "member_type", nullable = false, length = 50)
    private String memberType = "STUDENT";

    @Column(name = "card_number", nullable = false, length = 100)
    private String cardNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Staff staff;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "email")
    private String email;

    @Column(name = "phone", length = 50)
    private String phone;

    @Column(name = "class_section", length = 100)
    private String classSection;

    @Column(name = "admission_number", length = 100)
    private String admissionNumber;

    @Column(name = "max_books_allowed", nullable = false)
    private Integer maxBooksAllowed = 3;

    @Column(name = "active_issued_count", nullable = false)
    private Integer activeIssuedCount = 0;

    @Column(name = "status", nullable = false, length = 50)
    private String status = "Active";

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
