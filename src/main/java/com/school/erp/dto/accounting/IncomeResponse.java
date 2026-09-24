package com.school.erp.dto.accounting;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class IncomeResponse {
    private Long id;
    private IncomeHeadResponse incomeHead;
    private String accountType;
    private String accountName;
    private String incomeFrom;
    private String invoiceNumber;
    private BigDecimal amount;
    private LocalDate date;
    private String paymentMode;
    private String documentUrl;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
