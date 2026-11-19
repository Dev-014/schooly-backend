package com.school.erp.entity.finance;

import com.school.erp.entity.superadmin.School;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "gateway_transaction_log")
public class GatewayTransactionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "domain", nullable = false)
    private String domain; // 'STUDENT_FEE' or 'SCHOOL_SUBSCRIPTION'

    @Column(name = "reference_id", nullable = false)
    private Long referenceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id")
    private School school;

    @Column(name = "gateway", nullable = false)
    private String gateway = "RAZORPAY";

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(name = "gateway_order_id")
    private String gatewayOrderId;

    @Column(name = "gateway_payment_id")
    private String gatewayPaymentId;

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "raw_payload", columnDefinition = "jsonb")
    private String rawPayload;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}
