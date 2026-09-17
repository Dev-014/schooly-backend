package com.school.erp.dto.exam;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportCardQueueStatusResponse {
    private String batchName;
    private BigDecimal progressPercent; // 85.0
    private int processedCount; // 357
    private int totalCount; // 420
    private String status; // PROCESSING
    private boolean realTimeUpdate;
    private String description; // "357 of 420 reports generated successfully..."
}
