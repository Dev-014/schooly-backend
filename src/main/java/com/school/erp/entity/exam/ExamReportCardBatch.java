package com.school.erp.entity.exam;

import com.school.erp.entity.superadmin.School;
import com.school.erp.entity.academic.SchoolClass;
import com.school.erp.entity.academic.Section;
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
@Table(name = "exam_report_card_batches")
public class ExamReportCardBatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

    @Column(name = "batch_name", nullable = false, length = 150)
    private String batchName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "term_id")
    private ExamTerm term;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_setup_id")
    private ExamSetup examSetup;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id")
    private SchoolClass schoolClass;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id")
    private Section section;

    @Column(name = "generation_mode", nullable = false, length = 20)
    private String generationMode = "TERM_WISE";

    @Column(name = "template_name", length = 100)
    private String templateName = "Classic CBSE Standard";

    @Column(name = "total_count", nullable = false)
    private Integer totalCount = 0;

    @Column(name = "processed_count", nullable = false)
    private Integer processedCount = 0;

    @Column(name = "progress_percent", precision = 5, scale = 2, nullable = false)
    private BigDecimal progressPercent = BigDecimal.ZERO;

    @Column(name = "status", nullable = false, length = 30)
    private String status = "PROCESSING"; // PROCESSING, COMPLETED, FAILED

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
