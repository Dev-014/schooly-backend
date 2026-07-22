package com.school.erp.dto.catalog;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EntitlementEvaluationDto {
    private Long schoolId;
    private String activePlanCode;
    private String activePlanName;
    private Set<String> enabledModules;
    private Map<String, String> activeOverrides; // moduleCode -> overrideType
    private boolean isTrialActive;
    private LocalDateTime evaluatedAt;
}
