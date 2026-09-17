package com.school.erp.dto.exam;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class ExamSetupResponse {
    private Long id;
    private Long schoolId;
    private Long termId;
    private String termName;
    private Integer orderNo;
    private String name;
    private String groupName;
    private Integer bestOfCount;
    private Boolean weightageActive;
    private BigDecimal weightagePercent;
    private String evaluationType;
    private String internalNote;
    private String status;
    private Long scheduleCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
