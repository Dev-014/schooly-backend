package com.school.erp.dto.communication;

import com.school.erp.entity.communication.CommunicationChannel;
import com.school.erp.entity.communication.CommunicationDeliveryStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommunicationDeliveryDTO {
    private Long id;
    private Long announcementId;
    private Long schoolId;
    private String schoolName;
    private CommunicationChannel deliveryChannel;
    private CommunicationDeliveryStatus status;
    private LocalDateTime deliveredAt;
    private LocalDateTime readAt;
    private LocalDateTime createdAt;
}
