package com.school.erp.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class EmployeeNoteDTO {
    private Long id;
    private Long employeeId;
    private String noteContent;
    private Long authorId;
    private String authorName;
    private LocalDateTime createdAt;
}
