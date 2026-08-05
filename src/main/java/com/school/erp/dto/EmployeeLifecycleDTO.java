package com.school.erp.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class EmployeeLifecycleDTO {
    private Long id;
    private Long employeeId;
    private String eventType;
    private LocalDateTime eventDate;
    private String description;
    private Long createdBy;
    private String createdByName;
}
