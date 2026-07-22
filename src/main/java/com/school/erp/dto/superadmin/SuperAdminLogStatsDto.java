package com.school.erp.dto.superadmin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SuperAdminLogStatsDto {
    private Long totalEvents;
    private String eventsGrowth;
    private Long securityAlerts;
    private String alertsGrowth;
    private Double avgLatencyMs;
    private String latencyNote;
    private Long activeSessions;
    private String sessionsNote;
}
