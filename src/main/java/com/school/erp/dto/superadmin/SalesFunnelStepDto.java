package com.school.erp.dto.superadmin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalesFunnelStepDto {
    private String stage;
    private Long count;
    private Double conversionRate;
}
