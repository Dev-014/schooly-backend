package com.school.erp.dto.crm;

import com.school.erp.entity.crm.CrmDemoMode;
import com.school.erp.entity.crm.CrmDemoStatus;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CrmDemoDto {
    private Long id;
    private Long leadId;
    private LocalDateTime demoDate;
    private CrmDemoMode mode;
    private Long demoById;
    private String demoByName;
    private CrmDemoStatus status;
    private String feedback;
    private String recordingUrl;
    private String meetingNotes;
    private LocalDateTime createdAt;
}
