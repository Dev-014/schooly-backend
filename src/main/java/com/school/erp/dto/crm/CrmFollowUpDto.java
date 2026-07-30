package com.school.erp.dto.crm;

import com.school.erp.entity.crm.CrmFollowUpAction;
import com.school.erp.entity.crm.CrmFollowUpStatus;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CrmFollowUpDto {
    private Long id;
    private Long leadId;
    private CrmFollowUpAction actionType;
    private LocalDateTime scheduledDate;
    private String remarks;
    private Long executiveId;
    private String executiveName;
    private CrmFollowUpStatus status;
    private LocalDateTime createdAt;
}
