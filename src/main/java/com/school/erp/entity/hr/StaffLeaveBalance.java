package com.school.erp.entity.hr;

import com.school.erp.entity.School;
import com.school.erp.entity.Staff;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "staff_leave_balances")
public class StaffLeaveBalance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "staff_id", nullable = false)
    private Staff staff;

    @Column(name = "leave_type_id", nullable = false)
    private Long leaveTypeId;

    @Column(name = "total_leaves")
    private BigDecimal totalLeaves = BigDecimal.ZERO;

    @Column(name = "used_leaves")
    private BigDecimal usedLeaves = BigDecimal.ZERO;

    @Column(name = "remaining_leaves")
    private BigDecimal remainingLeaves = BigDecimal.ZERO;
}
