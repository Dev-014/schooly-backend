package com.school.erp.dto.superadmin;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class SchoolSubscriptionInstallmentDTO {
    private Long id;
    private Integer installmentNumber;
    private BigDecimal amount;
    private LocalDate dueDate;
    private String status;
    private LocalDate paidDate;
}
