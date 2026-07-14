package com.school.erp.dto.superadmin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConfigOverrideDto {
    private Integer studentLimit;
    private Boolean enableBetaFeatures;
}
