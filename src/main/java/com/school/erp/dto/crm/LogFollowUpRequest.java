package com.school.erp.dto.crm;
import com.school.erp.entity.crm.CrmFollowUpAction;
import lombok.Data;
import java.time.LocalDateTime;
@Data
public class LogFollowUpRequest {
    private CrmFollowUpAction actionType;
    private LocalDateTime scheduledDate;
    private String remarks;
}
