package com.school.erp.dto.feestructure;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class FeeStructureItemResponse {
    private Long id;
    private Long feeCategoryId;
    private String feeCategoryName;
    private BigDecimal amount;
    private Boolean isPartOfCollectionPlan;
}
