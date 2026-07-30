package com.school.erp.entity.crm;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "crm_quotations")
public class CrmQuotation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "quotation_number", nullable = false, unique = true)
    private String quotationNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lead_id", nullable = false)
    private CrmLead lead;

    @Column(name = "plan_name", nullable = false)
    private String planName;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(precision = 12, scale = 2)
    private BigDecimal discount = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    private BigDecimal gst = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal total;

    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private CrmQuotationStatus status = CrmQuotationStatus.DRAFT;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
