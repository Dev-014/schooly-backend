package com.school.erp.dto.accounting;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class IncomeHeadRequest {
    @NotBlank(message = "Name is required")
    private String name;
    private String description;
}
