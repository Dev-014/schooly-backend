package com.school.erp.dto.hr;

import lombok.Data;
import java.time.LocalDate;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class StaffLeaveDTO {
    private Long id;
    private Long schoolId;
    private Long staffId;
    private String staffName;
    private Long leaveTypeId;
    private String leaveTypeName;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal numberOfDays;
    private String reason;
    private String status;
    private Long approvedBy;
}
