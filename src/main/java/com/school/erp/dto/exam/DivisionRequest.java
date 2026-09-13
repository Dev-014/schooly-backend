package com.school.erp.dto.exam;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class DivisionRequest {

    @NotBlank(message = "Division name is required")
    private String name;

    @NotNull(message = "Percentage from is required")
    private BigDecimal percentFrom;

    @NotNull(message = "Percentage to is required")
    private BigDecimal percentTo;

    private String description;

    private String colorTag = "BLUE"; // GREEN, BLUE, ORANGE, RED

    private String status = "ACTIVE";

    private Integer orderNo = 1;
}
