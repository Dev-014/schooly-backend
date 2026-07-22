package com.school.erp.service.superadmin;

import com.school.erp.dto.superadmin.SystemConfigDto;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicReference;

@Service
public class SuperAdminConfigService {

    private final AtomicReference<SystemConfigDto> currentConfig = new AtomicReference<>(
            SystemConfigDto.builder()
                    .platformName("Schooly Autonomous ERP Main Cloud")
                    .supportEmail("superadmin@schooly.io")
                    .multiTenantDomain(".schooly.io")
                    .smtpHost("smtp.mailgun.org:587")
                    .smtpConnected(true)
                    .defaultCurrency("INR (₹)")
                    .maintenanceMode(false)
                    .automaticBackups(true)
                    .backupSchedule("Daily at 02:00 AM UTC")
                    .aiAssistantEnabled(true)
                    .auditRetentionDays(365)
                    .build()
    );

    public SystemConfigDto getConfig() {
        return currentConfig.get();
    }

    public SystemConfigDto updateConfig(SystemConfigDto newConfig) {
        SystemConfigDto updated = SystemConfigDto.builder()
                .platformName(newConfig.getPlatformName() != null ? newConfig.getPlatformName() : currentConfig.get().getPlatformName())
                .supportEmail(newConfig.getSupportEmail() != null ? newConfig.getSupportEmail() : currentConfig.get().getSupportEmail())
                .multiTenantDomain(newConfig.getMultiTenantDomain() != null ? newConfig.getMultiTenantDomain() : currentConfig.get().getMultiTenantDomain())
                .smtpHost(newConfig.getSmtpHost() != null ? newConfig.getSmtpHost() : currentConfig.get().getSmtpHost())
                .smtpConnected(newConfig.getSmtpConnected() != null ? newConfig.getSmtpConnected() : currentConfig.get().getSmtpConnected())
                .defaultCurrency(newConfig.getDefaultCurrency() != null ? newConfig.getDefaultCurrency() : currentConfig.get().getDefaultCurrency())
                .maintenanceMode(newConfig.getMaintenanceMode() != null ? newConfig.getMaintenanceMode() : currentConfig.get().getMaintenanceMode())
                .automaticBackups(newConfig.getAutomaticBackups() != null ? newConfig.getAutomaticBackups() : currentConfig.get().getAutomaticBackups())
                .backupSchedule(newConfig.getBackupSchedule() != null ? newConfig.getBackupSchedule() : currentConfig.get().getBackupSchedule())
                .aiAssistantEnabled(newConfig.getAiAssistantEnabled() != null ? newConfig.getAiAssistantEnabled() : currentConfig.get().getAiAssistantEnabled())
                .auditRetentionDays(newConfig.getAuditRetentionDays() != null ? newConfig.getAuditRetentionDays() : currentConfig.get().getAuditRetentionDays())
                .build();
        currentConfig.set(updated);
        return updated;
    }
}
