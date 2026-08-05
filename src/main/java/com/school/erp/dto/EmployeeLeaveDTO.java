package com.school.erp.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class EmployeeLeaveDTO {
    private Long id;
    private Long employeeId;
    private String employeeName;
    private String leaveType;
    private LocalDate startDate;
    private LocalDate endDate;
    private String reason;
    private String status;
    private Long approvedById;
    private String approvedByName;
}
