package com.school.erp.dto.superadmin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlatformHealthDto {
    private Double serverStatusPercentage;
    private Long apiResponseTimeMs;
    private Double applicationCrashRate;
    private Double storageUsedTb;
    private Double storageCapacityPercentage;
    private Long smsBalance;
    private String emailQueueStatus;
}
