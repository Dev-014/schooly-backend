package com.school.erp.dto.superadmin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModuleAdoptionDto {
    private String moduleName;
    private String moduleCode;
    private Long schoolsCount;
    private Double percentage;
}
