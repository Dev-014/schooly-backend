package com.school.erp.dto.finance;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CollectionPlanItemResponse {
    private Long id;
    private String label;
    private LocalDate dueDate;
    private String amountType;
    private BigDecimal amountValue;
    private Integer sequenceOrder;
}
