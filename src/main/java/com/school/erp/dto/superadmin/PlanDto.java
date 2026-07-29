package com.school.erp.dto.superadmin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanDto {
    private Long id;
    private String code;
    private String name;
    private BigDecimal monthlyPrice;
    private BigDecimal annualPrice;
    private BigDecimal pricePerStudent;
    private String billingModel;
    private String description;
    private String status;
    private List<String> features;
    private List<String> moduleCodes;
    private Limits limits;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Limits {
        private Integer maxStudents;
        private Integer storageGb;
    }
}
