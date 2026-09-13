package com.school.erp.dto.exam;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TeacherRemarkStatsResponse {
    private long totalRemarks;
    private long completedRemarks;
    private long pendingRemarks;
    private int completionPercentage;
}
