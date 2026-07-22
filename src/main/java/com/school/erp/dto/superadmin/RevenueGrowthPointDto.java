package com.school.erp.dto.superadmin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevenueGrowthPointDto {
    private String month;
    private BigDecimal mrr;
    private BigDecimal arr;
    private BigDecimal expenses;
}
