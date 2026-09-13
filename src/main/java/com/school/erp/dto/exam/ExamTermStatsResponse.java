package com.school.erp.dto.exam;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ExamTermStatsResponse {
    private long totalTerms;
    private long activeTerms;
    private long scheduledTerms;
    private long completedTerms;
    private String currentAcademicSession;
    private String systemHealthStatus;
}
