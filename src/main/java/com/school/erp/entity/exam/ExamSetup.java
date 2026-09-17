package com.school.erp.entity.exam;

import com.school.erp.entity.School;
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
@Table(name = "exam_setups")
public class ExamSetup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "term_id", nullable = false)
    private ExamTerm term;

    @Column(name = "order_no", nullable = false)
    private Integer orderNo = 1;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "group_name", length = 100)
    private String groupName;

    @Column(name = "best_of_count")
    private Integer bestOfCount;

    @Column(name = "weightage_active")
    private Boolean weightageActive = false;

    @Column(name = "weightage_percent", precision = 5, scale = 2)
    private BigDecimal weightagePercent = BigDecimal.ZERO;

    @Column(name = "evaluation_type", length = 50)
    private String evaluationType = "STANDARD";

    @Column(name = "internal_note", columnDefinition = "TEXT")
    private String internalNote;

    @Column(name = "status", nullable = false, length = 30)
    private String status = "ACTIVE";

    @CreationTimestamp
    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
