package com.school.erp.dto.catalog;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CatalogModuleDto {
    private Long id;
    private String code;
    private String name;
    private String description;
    private String category;
    private BigDecimal addOnPrice;
    private boolean isDefault;
    private String status;
}
