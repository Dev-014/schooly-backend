package com.school.erp.dto.payment;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class FeeReminderResponse {
    private Long id;
    private Long studentId;
    private String studentName;
    private String parentName;
    private BigDecimal feeAmount;
    private String method;
    private String status;
    private LocalDateTime sentAt;
}
