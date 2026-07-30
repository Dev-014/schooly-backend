package com.school.erp.dto.crm;

import lombok.Data;

@Data
public class CrmDashboardStatsDto {
    private long totalLeads;
    private long newLeads;
    private long todaysFollowUps;
    private long upcomingDemos;
    private long quotationsSent;
    private long negotiations;
    private long wonDeals;
    private long lostDeals;
    private double conversionRate;
    private double monthlyRevenue;
}
