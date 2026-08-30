package com.school.erp.dto.finance;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CollectionPlanItemRequest {
    @NotBlank(message = "Label is required")
    private String label;
    
    private LocalDate dueDate;
    
    @NotBlank(message = "Amount type is required")
    private String amountType;
    
    private BigDecimal amountValue;
    
    private Integer sequenceOrder = 0;
}
