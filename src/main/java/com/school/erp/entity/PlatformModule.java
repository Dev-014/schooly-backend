package com.school.erp.entity;

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
@Table(name = "platform_modules")
public class PlatformModule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = false, unique = true)
    private String code;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "category")
    private String category = "CORE";

    @Column(name = "add_on_price", precision = 10, scale = 2)
    private java.math.BigDecimal addOnPrice = java.math.BigDecimal.ZERO;

    @Column(name = "is_default")
    private Boolean isDefault = false;

    public boolean isDefault() {
        return Boolean.TRUE.equals(isDefault);
    }

    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }

    @Column(name = "status", nullable = false)
    private String status = "ACTIVE";

    @CreationTimestamp
    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;
}
