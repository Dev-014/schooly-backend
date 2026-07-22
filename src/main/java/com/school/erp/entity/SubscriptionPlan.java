package com.school.erp.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "subscription_plans")
public class SubscriptionPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", unique = true)
    private String code;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "monthly_price", precision = 10, scale = 2)
    private BigDecimal monthlyPrice;

    @Column(name = "annual_price", precision = 10, scale = 2)
    private BigDecimal annualPrice;

    @Column(name = "max_students")
    private Integer maxStudents;

    @Column(name = "storage_gb")
    private Integer storageGb;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "features", columnDefinition = "TEXT")
    private String features;

    @Column(name = "status", nullable = false)
    private String status = "ACTIVE";

    @ManyToMany
    @JoinTable(
        name = "plan_modules",
        joinColumns = @JoinColumn(name = "plan_id"),
        inverseJoinColumns = @JoinColumn(name = "module_id")
    )
    private Set<PlatformModule> modules = new HashSet<>();

    @CreationTimestamp
    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;
}
