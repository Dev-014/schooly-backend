package com.school.erp.dto.exam;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ExamScheduleStatsResponse {
    private long upcomingExams;
    private long totalSubjects;
    private int venueCapacityPercentage;
}
