package com.school.erp.dto.feestructure;

import lombok.Data;
import java.math.BigDecimal;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Data
public class FeeStructureItemRequest {
    @NotNull(message = "Fee category ID is required")
    private Long feeCategoryId;
    
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be greater than zero")
    private BigDecimal amount;
    
    private Boolean isPartOfCollectionPlan = true;
}
