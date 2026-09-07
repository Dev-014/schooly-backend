package com.school.erp.dto.student;

import java.math.BigDecimal;

/**
 * Aggregated fee summary for the student self-service portal.
 */
public record StudentFeeSummaryResponse(
        BigDecimal totalFee,
        BigDecimal totalPaid,
        BigDecimal pendingAmount,
        String overallStatus,   // PAID, PARTIALLY_PAID, PENDING, OVERDUE
        String nextDueDate      // ISO-8601 date string, or null if all paid
) {
}
