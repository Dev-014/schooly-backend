package com.school.erp.entity.exam;

import com.school.erp.entity.School;
import com.school.erp.entity.SchoolClass;
import com.school.erp.entity.Section;
import com.school.erp.entity.Student;
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
@Table(name = "exam_report_cards")
public class ExamReportCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "term_id", nullable = false)
    private ExamTerm term;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_setup_id")
    private ExamSetup examSetup;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "class_id", nullable = false)
    private SchoolClass schoolClass;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id")
    private Section section;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(name = "generation_mode", nullable = false, length = 20)
    private String generationMode = "TERM_WISE"; // TERM_WISE, EXAM_WISE

    @Column(name = "template_name", length = 100)
    private String templateName = "Classic CBSE Standard";

    @Column(name = "total_marks", precision = 6, scale = 2)
    private BigDecimal totalMarks;

    @Column(name = "max_marks", precision = 6, scale = 2)
    private BigDecimal maxMarks = new BigDecimal("100.00");

    @Column(name = "percentage", precision = 5, scale = 2)
    private BigDecimal percentage;

    @Column(name = "grade", length = 10)
    private String grade;

    @Column(name = "division", length = 50)
    private String division;

    @Column(name = "status", nullable = false, length = 30)
    private String status = "GENERATED"; // GENERATED, ISSUED, PENDING

    @Column(name = "batch_id")
    private Long batchId;

    @Column(name = "file_url", columnDefinition = "TEXT")
    private String fileUrl;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
