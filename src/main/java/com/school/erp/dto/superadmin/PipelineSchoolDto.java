package com.school.erp.dto.superadmin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PipelineSchoolDto {
    private Long id;
    private String name;
    private String location;
    private String plan;
    private String health;
    private String status;
    private String onboardingStep;
    private String updatedAt;
}
