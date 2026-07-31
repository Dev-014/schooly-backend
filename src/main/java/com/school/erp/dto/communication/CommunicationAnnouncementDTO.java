package com.school.erp.dto.communication;

import com.school.erp.entity.communication.CommunicationAudienceType;
import com.school.erp.entity.communication.CommunicationImportance;
import com.school.erp.entity.communication.CommunicationMessageType;
import com.school.erp.entity.communication.CommunicationStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CommunicationAnnouncementDTO {
    private Long id;
    private String subject;
    private String message;
    private CommunicationMessageType messageType;
    private CommunicationImportance importance;
    private CommunicationStatus status;
    private CommunicationAudienceType audienceType;
    private List<String> audienceCriteria;
    private LocalDateTime scheduledAt;
    private Long createdByUserId;
    private String createdByUserName;
    private LocalDateTime createdAt;
    
    // Stats
    private long totalDeliveries;
    private long deliveredCount;
    private long readCount;
    private long failedCount;
}
