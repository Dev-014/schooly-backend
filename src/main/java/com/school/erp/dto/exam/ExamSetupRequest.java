package com.school.erp.dto.exam;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ExamSetupRequest {

    @NotNull(message = "Term ID is required")
    private Long termId;

    private Integer orderNo = 1;

    @NotBlank(message = "Exam name is required")
    private String name;

    private String groupName;

    private Integer bestOfCount;

    private Boolean weightageActive = false;

    private BigDecimal weightagePercent = BigDecimal.ZERO;

    private String evaluationType = "STANDARD";

    private String internalNote;

    private String status = "ACTIVE";
}
