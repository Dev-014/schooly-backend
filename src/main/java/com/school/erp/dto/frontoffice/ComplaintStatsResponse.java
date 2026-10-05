package com.school.erp.dto.frontoffice;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComplaintStatsResponse {

    private long totalComplaints;
    private long openComplaints;
    private long closedComplaints;
    private long thisMonthResolved;
}
