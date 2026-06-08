package com.school.erp.dto.dashboard;

import java.math.BigDecimal;

public record DashboardKpiResponse(
        BigDecimal monthlyCollection,
        BigDecimal monthlyExpense,
        BigDecimal projectedExpense,
        long newAdmissions,
        long staffPresent,
        long staffTotal,
        long studentPresent,
        long studentTotal,
        String collectionTrend
) {
}
