package com.school.erp.dto.communication;

import com.school.erp.entity.communication.CommunicationMessageType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateTemplateRequest {
    @NotBlank(message = "Template name is required")
    private String templateName;

    @NotNull(message = "Category is required")
    private CommunicationMessageType category;

    @NotBlank(message = "Message is required")
    private String message;
}
