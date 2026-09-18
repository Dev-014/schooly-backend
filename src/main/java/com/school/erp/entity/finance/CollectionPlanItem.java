package com.school.erp.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "collection_plan_item")
public class CollectionPlanItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "collection_plan_id", nullable = false)
    private CollectionPlan collectionPlan;

    @Column(nullable = false)
    private String label;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "amount_type", nullable = false)
    private String amountType; // FIXED, PERCENTAGE, REMAINDER

    @Column(name = "amount_value")
    private BigDecimal amountValue;

    @Column(name = "sequence_order", nullable = false)
    private Integer sequenceOrder = 0;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}
