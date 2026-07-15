package com.school.erp.dto.superadmin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlanDto {
    private Long id;
    private String name;
    private BigDecimal monthlyPrice;
    private List<String> moduleCodes;
    private Limits limits;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Limits {
        private Integer maxStudents;
        private Integer storageGb;
    }
}
