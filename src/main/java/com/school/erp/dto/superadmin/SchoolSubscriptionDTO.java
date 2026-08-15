package com.school.erp.dto.superadmin;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class SchoolSubscriptionDTO {
    private Long id;
    private Long schoolId;
    private String planName;
    private String billingPeriod;
    private Integer totalStudents;
    private BigDecimal totalAmount;
    private BigDecimal amountPaid;
    private BigDecimal remainingAmount;
    private String status;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<SchoolSubscriptionInstallmentDTO> installments;
}
