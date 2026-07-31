package com.school.erp.dto.communication;

import lombok.Data;

@Data
public class CommunicationDashboardStatsDTO {
    private long messagesSentToday;
    private long scheduledMessages;
    private long deliveredToday;
    private long readToday;
    private long unreadToday;
    private long failedToday;
}
