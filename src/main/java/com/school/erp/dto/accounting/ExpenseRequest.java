package com.school.erp.dto.accounting;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ExpenseRequest {
    @NotNull(message = "Expense head ID is required")
    private Long expenseHeadId;
    
    @NotBlank(message = "Account type is required")
    private String accountType;
    
    private String accountName;
    private String vendorName;
    private String invoiceNumber;
    
    @NotNull(message = "Amount is required")
    private BigDecimal amount;
    
    @NotNull(message = "Date is required")
    private LocalDate date;
    
    private String paymentMode;
    private String documentUrl;
    private String description;
}
