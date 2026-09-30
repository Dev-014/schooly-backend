package com.school.erp.entity.library;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.school.erp.entity.superadmin.School;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "library_circulations")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class LibraryCirculation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "school_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private School school;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private LibraryBook book;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private LibraryMember member;

    @Column(name = "issue_date", nullable = false)
    private LocalDate issueDate = LocalDate.now();

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "return_date")
    private LocalDate returnDate;

    @Column(name = "fine_amount", precision = 10, scale = 2)
    private BigDecimal fineAmount = BigDecimal.ZERO;

    @Column(name = "fine_paid", nullable = false)
    private Boolean finePaid = false;

    @Column(name = "status", nullable = false, length = 50)
    private String status = "Issued"; // Issued, Returned, Overdue, Lost

    @Column(name = "issued_by", length = 100)
    private String issuedBy;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
