package com.school.erp.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class EmployeeTimelineDTO {
    private Long id;
    private Long employeeId;
    private String title;
    private String description;
    private LocalDateTime date;
}
