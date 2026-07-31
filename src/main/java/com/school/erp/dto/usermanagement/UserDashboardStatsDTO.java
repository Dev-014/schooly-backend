package com.school.erp.dto.usermanagement;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDashboardStatsDTO {
    private long totalUsers;
    private long activeUsers;
    private long inactiveUsers;
    private long lockedAccounts;
    private long todaysLogins;
    private long passwordResetRequests;
    private long newUsersThisMonth;
    private long onlineUsers;
    private List<ChartData> usersByRole;
    private List<ChartData> usersByStatus;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChartData {
        private String label;
        private long value;
    }
}
