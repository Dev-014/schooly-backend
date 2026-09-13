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
@Table(name = "exam_divisions")
public class ExamDivision {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "percent_from", precision = 5, scale = 2, nullable = false)
    private BigDecimal percentFrom;

    @Column(name = "percent_to", precision = 5, scale = 2, nullable = false)
    private BigDecimal percentTo;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "color_tag", length = 20)
    private String colorTag = "BLUE";

    @Column(name = "status", nullable = false, length = 20)
    private String status = "ACTIVE";

    @Column(name = "order_no")
    private Integer orderNo = 1;

    @CreationTimestamp
    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
