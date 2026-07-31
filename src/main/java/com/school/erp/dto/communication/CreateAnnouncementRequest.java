package com.school.erp.dto.communication;

import com.school.erp.entity.communication.CommunicationAudienceType;
import com.school.erp.entity.communication.CommunicationChannel;
import com.school.erp.entity.communication.CommunicationImportance;
import com.school.erp.entity.communication.CommunicationMessageType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
public class CreateAnnouncementRequest {

    @NotBlank(message = "Subject is required")
    private String subject;

    @NotBlank(message = "Message is required")
    private String message;

    @NotNull(message = "Message type is required")
    private CommunicationMessageType messageType;

    @NotNull(message = "Importance is required")
    private CommunicationImportance importance;

    @NotNull(message = "Audience type is required")
    private CommunicationAudienceType audienceType;

    private List<String> audienceCriteria;
    
    // For specific schools (if SELECTED_SCHOOLS is chosen)
    private Set<Long> specificSchoolIds;

    @NotNull(message = "Delivery channels are required")
    private Set<CommunicationChannel> deliveryChannels;

    private Boolean scheduleForLater = false;
    
    private Boolean saveAsDraft = false;

    private LocalDateTime scheduledAt;
}
