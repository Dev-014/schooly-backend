package com.school.erp.entity.hr;

import com.school.erp.entity.School;
import com.school.erp.entity.Staff;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "staff_advances")
public class StaffAdvance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "staff_id", nullable = false)
    private Staff staff;

    @Column(name = "advance_date", nullable = false)
    private LocalDate advanceDate;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    @Column(name = "repayment_method")
    private String repaymentMethod;

    @Column(name = "status")
    private String status = "PENDING"; // PENDING, PARTIALLY_RECOVERED, FULLY_RECOVERED

    @Column(name = "recovered_amount")
    private BigDecimal recoveredAmount = BigDecimal.ZERO;
}
