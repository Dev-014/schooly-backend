package com.school.erp.dto.accounting;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ExpenseResponse {
    private Long id;
    private ExpenseHeadResponse expenseHead;
    private String accountType;
    private String accountName;
    private String vendorName;
    private String invoiceNumber;
    private BigDecimal amount;
    private LocalDate date;
    private String paymentMode;
    private String documentUrl;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
