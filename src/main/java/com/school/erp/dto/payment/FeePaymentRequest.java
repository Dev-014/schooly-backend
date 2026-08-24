package com.school.erp.dto.payment;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class FeePaymentRequest {
    @NotNull(message = "studentId is required")
    private Long studentId;
    
    private Long schoolId;
    
    @NotNull(message = "invoiceId is required")
    private Long invoiceId;
    
    @NotNull(message = "feeItemIds is required")
    private List<Long> feeItemIds;
    
    @NotNull(message = "totalAmount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "totalAmount must be greater than zero")
    private BigDecimal totalAmount;
    
    private LocalDate paymentDate;
    
    @NotNull(message = "paymentMethod is required")
    private String paymentMethod;
    
    private Boolean sendSmsReceipt;
}
