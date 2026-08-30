package com.school.erp.dto.payment;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FeeStatsDTO {
    private BigDecimal totalExpected;
    private BigDecimal totalCollected;
    private BigDecimal partialAmount;
    private BigDecimal overdueBalance;
    private Long partialCount;
}
