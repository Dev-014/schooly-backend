package com.school.erp.dto.superadmin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ModuleDto {
    private Long id;
    private String code;
    private String name;
    private String description;
    private boolean isDefault;
    private String status;
}
