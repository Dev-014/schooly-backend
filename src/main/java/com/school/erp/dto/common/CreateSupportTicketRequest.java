package com.school.erp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateSupportTicketRequest {
    @NotBlank
    private String portalSource;
    
    @NotNull
    private Long categoryId;
    
    private String priority;
    
    @NotBlank
    private String subject;
    
    @NotBlank
    private String description;
    
    private String attachmentUrl;
}
