package com.school.erp.dto.exam;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamSessionSummaryResponse {
    private String roomNumber;
    private String sessionStatus; // ACTIVE, COMPLETED
    private long presentCount;
    private long absentCount;
    private long leaveCount;
    private long totalExaminees;
    private long hallCapacity;
    private String upcomingExam;
}
