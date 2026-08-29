package com.school.erp.dto.finance;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class FeeInstallmentItemResponse {
    private Long id;
    private Long feeCategoryId;
    private String categoryName;
    private BigDecimal amount;
}
