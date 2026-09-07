package com.school.erp.entity.hr;

import com.school.erp.entity.School;
import com.school.erp.entity.Staff;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;
import java.math.BigDecimal;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "staff_leaves")
public class StaffLeave {
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

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "number_of_days", nullable = false)
    private BigDecimal numberOfDays;

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    @Column(name = "status")
    private String status = "PENDING";

    @Column(name = "approved_by")
    private Long approvedBy;
}
