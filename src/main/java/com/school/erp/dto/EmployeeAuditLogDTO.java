package com.school.erp.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class EmployeeAuditLogDTO {
    private Long id;
    private Long employeeId;
    private String employeeName;
    private String action;
    private String entityType;
    private Long entityId;
    private String details;
    private LocalDateTime createdAt;
}
