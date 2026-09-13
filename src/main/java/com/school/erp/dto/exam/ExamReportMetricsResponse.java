package com.school.erp.dto.exam;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamReportMetricsResponse {
    private long totalStudents; // 124
    private String totalStudentsTrend; // "+12% from Mid-Term"

    private BigDecimal passPercentage; // 94.2
    private String passPercentageTrend; // "2.1% improvement"

    private BigDecimal classAverage; // 78.5
    private String classAverageTrend; // "Stable Performance"

    private BigDecimal highestScore; // 98.4
    private String highestScorerName; // "Liam R. Anderson"
    private String distinctionBadge; // "Distinction Achieved"
}
