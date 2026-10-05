package com.school.erp.dto.hr;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffAdvanceDTO {
    private Long id;
    private Long schoolId;
    private Long staffId;
    private String staffName;
    private LocalDate advanceDate;
    private BigDecimal amount;
    private String reason;
    private String repaymentMethod;
    private String status;
    private BigDecimal recoveredAmount;
}
