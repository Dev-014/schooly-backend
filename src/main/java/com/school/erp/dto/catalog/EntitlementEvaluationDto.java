package com.school.erp.dto.catalog;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
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
    private Map<String, List<String>> disabledSubModules = new HashMap<>();

    public EntitlementEvaluationDto(
            Long schoolId,
            String activePlanCode,
            String activePlanName,
            Set<String> enabledModules,
            Map<String, String> activeOverrides,
            boolean isTrialActive,
            LocalDateTime evaluatedAt
    ) {
        this(schoolId, activePlanCode, activePlanName, enabledModules, activeOverrides, isTrialActive, evaluatedAt, new HashMap<>());
    }
}
