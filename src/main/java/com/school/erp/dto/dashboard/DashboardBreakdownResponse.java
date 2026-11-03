package com.school.erp.dto.dashboard;

import java.math.BigDecimal;

public class DashboardBreakdownResponse {

    public record EarningModeItem(String mode, BigDecimal amount, double percentage) {}

    public record FeeDistributionItem(String category, BigDecimal amount, double percentage) {}

    public record ClassDistributionItem(Long classId, String className, long studentCount, double percentage) {}

    public record StudentBirthdayItem(Long studentId, String studentName, String className, String dateOfBirth) {}
}

