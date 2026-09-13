package com.school.erp.dto.exam;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecentlyGeneratedBatchResponse {
    private Long id;
    private String title; // "Grade 10 - Sec B"
    private String relativeTime; // "Generated 5m ago"
    private String iconColor; // "GREEN", "PURPLE"
    private String status; // "COMPLETED"
}
