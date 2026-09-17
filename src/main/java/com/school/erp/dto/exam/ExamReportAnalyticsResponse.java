package com.school.erp.dto.exam;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamReportAnalyticsResponse {
    private BigDecimal avgClassPerformance; // e.g. 84.2
    private String avgClassPerformanceTrend; // e.g. "up"
    private List<TermPerformanceItem> termReports;
    private List<SubjectQuadrantItem> subjectBreakdown;
}
