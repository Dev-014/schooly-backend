package com.school.erp.dto.superadmin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemConfigDto {
    private String platformName;
    private String supportEmail;
    private String multiTenantDomain;
    private String smtpHost;
    private Boolean smtpConnected;
    private String defaultCurrency;
    private Boolean maintenanceMode;
    private Boolean automaticBackups;
    private String backupSchedule;
    private Boolean aiAssistantEnabled;
    private Integer auditRetentionDays;
}
