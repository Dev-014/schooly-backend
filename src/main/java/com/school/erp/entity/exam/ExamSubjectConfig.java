package com.school.erp.entity.exam;

import com.school.erp.entity.superadmin.School;
import com.school.erp.entity.academic.Subject;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(
    name = "exam_subject_configs",
    uniqueConstraints = @UniqueConstraint(columnNames = {"school_id", "exam_setup_id", "subject_id"})
)
public class ExamSubjectConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exam_setup_id", nullable = false)
    private ExamSetup examSetup;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @Column(name = "max_marks", precision = 6, scale = 2, nullable = false)
    private BigDecimal maxMarks = new BigDecimal("100.00");

    @Column(name = "passing_marks", precision = 6, scale = 2, nullable = false)
    private BigDecimal passingMarks = new BigDecimal("35.00");

    @Column(name = "theory_marks", precision = 6, scale = 2)
    private BigDecimal theoryMarks;

    @Column(name = "practical_marks", precision = 6, scale = 2)
    private BigDecimal practicalMarks;

    @Column(name = "internal_marks", precision = 6, scale = 2)
    private BigDecimal internalMarks;

    @CreationTimestamp
    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
