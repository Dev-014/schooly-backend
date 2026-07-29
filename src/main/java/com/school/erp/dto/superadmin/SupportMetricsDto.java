package com.school.erp.dto.superadmin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupportMetricsDto {
    private Long openTickets;
    private Long highPriorityTickets;
    private Long waitingOnCustomerTickets;
    private Long resolvedToday;
    private Double npsScore;
    private Long recentNegativeFeedback;
}
