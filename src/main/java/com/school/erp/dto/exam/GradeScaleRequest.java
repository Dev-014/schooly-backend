package com.school.erp.dto.exam;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class GradeScaleRequest {

    @NotBlank(message = "Grade name is required")
    private String name;

    private BigDecimal gradePoint = BigDecimal.ZERO;

    private String targetClass = "All Classes";

    @NotNull(message = "Percentage from is required")
    private BigDecimal percentFrom;

    @NotNull(message = "Percentage to is required")
    private BigDecimal percentTo;

    private String description;

    private String status = "ACTIVE";

    private Integer orderNo = 1;
}
