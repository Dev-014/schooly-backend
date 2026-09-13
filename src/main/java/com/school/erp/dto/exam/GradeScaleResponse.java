package com.school.erp.dto.exam;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class GradeScaleResponse {
    private Long id;
    private Long schoolId;
    private String name;
    private BigDecimal gradePoint;
    private String targetClass;
    private BigDecimal percentFrom;
    private BigDecimal percentTo;
    private String description;
    private String status;
    private Integer orderNo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
