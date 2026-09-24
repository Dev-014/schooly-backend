package com.school.erp.dto.accounting;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class IncomeRequest {
    @NotNull(message = "Income head ID is required")
    private Long incomeHeadId;
    
    @NotBlank(message = "Account type is required")
    private String accountType;
    
    private String accountName;
    private String incomeFrom;
    private String invoiceNumber;
    
    @NotNull(message = "Amount is required")
    private BigDecimal amount;
    
    @NotNull(message = "Date is required")
    private LocalDate date;
    
    private String paymentMode;
    private String documentUrl;
    private String description;
}
