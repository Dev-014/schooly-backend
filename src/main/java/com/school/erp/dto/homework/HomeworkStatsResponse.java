package com.school.erp.dto.homework;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HomeworkStatsResponse {

    private long totalAssigned;
    private long totalSubmissions;
    private long pendingEvaluation;
    private double completionRate;
    private BigDecimal averageScore;
}
