package com.school.erp.dto.exam;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ClassMarksStatsResponse {
    private BigDecimal averageMarks;
    private BigDecimal highestMarks;
    private long belowFortyCount;
    private int percentEntered;
    private long totalStudents;
    private long enteredCount;
}
