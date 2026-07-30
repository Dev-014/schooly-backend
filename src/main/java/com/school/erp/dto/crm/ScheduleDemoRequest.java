package com.school.erp.dto.crm;
import com.school.erp.entity.crm.CrmDemoMode;
import lombok.Data;
import java.time.LocalDateTime;
@Data
public class ScheduleDemoRequest {
    private LocalDateTime demoDate;
    private CrmDemoMode mode;
    private String meetingNotes;
}
