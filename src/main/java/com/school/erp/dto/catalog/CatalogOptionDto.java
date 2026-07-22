package com.school.erp.dto.catalog;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CatalogOptionDto {
    private String code;
    private String label;
    private String description;
    private boolean isDefault;
}
