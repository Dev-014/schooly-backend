package com.school.erp.dto.catalog;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

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
    @JsonProperty("isDefault")
    private Boolean isDefault;
    private String status;
    private List<String> targetRoles;
    private List<String> subModules;

    @JsonProperty("isDefault")
    public boolean isDefault() {
        return Boolean.TRUE.equals(isDefault);
    }
}
