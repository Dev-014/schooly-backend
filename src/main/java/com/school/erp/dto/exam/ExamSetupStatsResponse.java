package com.school.erp.dto.exam;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ExamSetupStatsResponse {
    private long activeExamsCount;
    private long addedThisTermCount;
    private BigDecimal avgWeightagePercent;
    private long pendingSetupCount;
}
