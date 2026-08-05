package com.school.erp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UploadDocumentRequest {
    @NotNull(message = "Employee ID is required")
    private Long employeeId;
    
    @NotBlank(message = "Document type is required")
    private String documentType;
    
    @NotBlank(message = "File name is required")
    private String fileName;
    
    @NotBlank(message = "File URL is required")
    private String fileUrl;
    
    private Long uploadedBy;
}
