package com.school.erp.dto.hr;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffLeaveBalanceDTO {
    private Long id;
    private Long schoolId;
    private Long staffId;
    private String staffName;
    private Long leaveTypeId;
    private String leaveTypeName;
    private BigDecimal totalLeaves;
    private BigDecimal usedLeaves;
    private BigDecimal remainingLeaves;
}
