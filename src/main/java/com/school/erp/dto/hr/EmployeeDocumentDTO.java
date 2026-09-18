package com.school.erp.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class EmployeeDocumentDTO {
    private Long id;
    private Long employeeId;
    private String employeeName;
    private String documentType;
    private String fileName;
    private String fileUrl;
    private Long uploadedBy;
    private String uploaderName;
    private String status;
    private LocalDateTime uploadedAt;
}
