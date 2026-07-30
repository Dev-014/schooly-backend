package com.school.erp.dto.crm;

import com.school.erp.entity.crm.CrmQuotationStatus;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CrmQuotationDto {
    private Long id;
    private String quotationNumber;
    private Long leadId;
    private String planName;
    private BigDecimal amount;
    private BigDecimal discount;
    private BigDecimal gst;
    private BigDecimal total;
    private LocalDateTime expiryDate;
    private CrmQuotationStatus status;
    private LocalDateTime createdAt;
}
