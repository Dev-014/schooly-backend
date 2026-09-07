package com.school.erp.entity.hr;

import com.school.erp.entity.School;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "school_leave_types")
public class SchoolLeaveType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "days_allowed")
    private Integer daysAllowed = 0;

    @Column(name = "is_paid")
    private Boolean isPaid = true;

    @Column(name = "applicable_roles")
    private String applicableRoles;
}
