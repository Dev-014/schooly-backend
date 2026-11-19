package com.school.erp.entity.finance;

import com.school.erp.entity.student.Student;
import com.school.erp.entity.superadmin.School;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "fee_payment")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private SchoolAccount account;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Column(name = "unallocated_amount")
    private BigDecimal unallocatedAmount = BigDecimal.ZERO;

    @Column(name = "payment_mode", nullable = false)
    private String paymentMode;

    @Column(name = "transaction_id")
    private String transactionId;

    @Column(name = "status")
    private String status;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;



    @Column(name = "receipt_number")
    private String receiptNumber;

    @Column(name = "payment_date")
    private LocalDate paymentDate;

    @Column(name = "gateway_order_id")
    private String gatewayOrderId;

    @Column(name = "gateway_payment_id")
    private String gatewayPaymentId;

    @Column(name = "gateway_signature")
    private String gatewaySignature;

    @Column(name = "verification_status")
    private String verificationStatus = "APPROVED"; // APPROVED, PENDING_VERIFICATION, REJECTED

    @Column(name = "verification_note", columnDefinition = "TEXT")
    private String verificationNote;

    @Column(name = "verified_by")
    private Long verifiedBy;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;

    @Column(name = "currency")
    private String currency = "INR";

    @Column(name = "receipt_file_id")
    private java.util.UUID receiptFileId;

    @OneToMany(mappedBy = "payment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FeePaymentAllocation> allocations = new ArrayList<>();
}
