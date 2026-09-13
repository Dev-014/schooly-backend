package com.school.erp.dto.exam;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GradeScaleStatsResponse {
    private long totalDefinitions;
    private int passThresholdPercent;
    private String avgPerformanceGrade;
}
