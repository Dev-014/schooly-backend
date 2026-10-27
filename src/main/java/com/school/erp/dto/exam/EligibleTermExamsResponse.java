package com.school.erp.dto.exam;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EligibleTermExamsResponse {
    private Long termId;
    private String termName;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean isCurrent;
    private List<EligibleExamOption> exams;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EligibleExamOption {
        private Long id;
        private String name;
        private String groupName;
        private String evaluationType;
        private String status;
        private Integer orderNo;
    }
}
