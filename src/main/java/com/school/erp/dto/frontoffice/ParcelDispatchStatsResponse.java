package com.school.erp.dto.frontoffice;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParcelDispatchStatsResponse {

    private long todayDispatches;
    private double percentageChangeFromYesterday;
    private long pendingDelivery;
    private long pendingDeliveryCouriersCount;
    private long successfulDeliveriesThisMonth;
}
