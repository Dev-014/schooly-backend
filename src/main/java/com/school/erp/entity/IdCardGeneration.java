package com.school.erp.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "id_card_generations")
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
public class IdCardGeneration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(name = "status", nullable = false)
    private String status = "GENERATED"; // GENERATED, PRINTED

    @CreatedDate
    @Column(name = "generated_at", nullable = false, updatable = false)
    private LocalDateTime generatedAt;
}
