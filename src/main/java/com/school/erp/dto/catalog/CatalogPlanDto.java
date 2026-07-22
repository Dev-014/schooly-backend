package com.school.erp.dto.catalog;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CatalogPlanDto {
    private Long id;
    private String code;
    private String name;
    private BigDecimal monthlyPrice;
    private BigDecimal annualPrice;
    private Integer maxStudents;
    private Integer storageGb;
    private String description;
    private List<String> features;
    private List<String> bundledModuleCodes;
    private String status;
}
