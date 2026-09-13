package com.school.erp.dto.exam;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CoCurricularStatsResponse {
    private long totalStudents;
    private long completedCount;
    private long pendingCount;
    private long missingEntriesCount;
}
