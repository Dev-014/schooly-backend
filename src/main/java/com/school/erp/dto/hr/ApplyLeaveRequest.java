package com.school.erp.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Data
public class ApplyLeaveRequest {
    @NotNull(message = "Employee ID is required")
    private Long employeeId;
    
    @NotNull(message = "Leave Type is required")
    private String leaveType;
    
    @NotNull(message = "Start Date is required")
    private LocalDate startDate;
    
    @NotNull(message = "End Date is required")
    private LocalDate endDate;
    
    private String reason;
}
