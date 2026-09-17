package com.school.erp.dto.exam;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdmitCardStatsResponse {
    private long totalStudents;
    private long generatedCount;
    private long pendingCount;
    private long releasedCount;
}
