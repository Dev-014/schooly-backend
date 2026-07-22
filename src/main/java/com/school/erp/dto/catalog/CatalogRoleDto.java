package com.school.erp.dto.catalog;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CatalogRoleDto {
    private String code;
    private String name;
    private String description;
    private boolean isSystemDefault;
    private List<String> defaultPermissions;
}
