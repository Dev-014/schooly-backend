package com.school.erp.dto.common;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TicketDashboardStatsDTO {
    private long newTickets;
    private long workingTickets;
    private long solvedToday;
    private long closedToday;
    private long waitingForSchool;
}
