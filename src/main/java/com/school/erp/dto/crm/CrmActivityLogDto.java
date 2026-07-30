package com.school.erp.dto.crm;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CrmActivityLogDto {
    private Long id;
    private Long leadId;
    private Long actorId;
    private String actorName;
    private String activityType;
    private String description;
    private String metadata;
    private LocalDateTime createdAt;
}
