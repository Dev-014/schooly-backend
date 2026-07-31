package com.school.erp.dto.communication;

import com.school.erp.entity.communication.CommunicationMessageType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommunicationTemplateDTO {
    private Long id;
    private String templateName;
    private CommunicationMessageType category;
    private String message;
    private Boolean isActive;
    private LocalDateTime createdAt;
}
