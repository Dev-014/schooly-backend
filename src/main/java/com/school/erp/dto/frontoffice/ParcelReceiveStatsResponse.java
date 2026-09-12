package com.school.erp.dto.frontoffice;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParcelReceiveStatsResponse {

    private long todayReceived;
    private long changeFromYesterday;
    private long pendingPickup;
    private long completedThisMonth;
}
