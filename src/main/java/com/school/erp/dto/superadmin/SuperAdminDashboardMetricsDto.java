package com.school.erp.dto.superadmin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SuperAdminDashboardMetricsDto {
    private Long totalSchools;
    private Long activeTenants;
    private BigDecimal totalArr;
    private BigDecimal monthlyRecurringRevenue;
    private Double systemHealthPercentage;
    private Double storageUsedGb;
    private Long activeStudents;
    private Long activeStaff;
    private Long pendingOnboardings;
    private String schoolsGrowth;
    private String arrGrowth;
}
