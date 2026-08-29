package com.school.erp.dto.finance;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SchoolAccountRequest {
    @NotBlank(message = "Account name is required")
    private String accountName;

    @NotBlank(message = "Account type is required (BANK, UPI, CASH)")
    private String accountType;

    private Boolean isActive = true;
}
