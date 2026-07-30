package com.school.erp.dto.crm;

import com.school.erp.entity.crm.CrmLeadSource;
import com.school.erp.entity.crm.CrmPipelineStage;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class CrmLeadDto {
    private Long id;
    private String schoolName;
    private String principalName;
    private String city;
    private String board;
    private String mobile;
    private String alternativeMobile;
    private String email;
    private String address;
    private String state;
    private String pinCode;
    private Integer approxStudentStrength;
    private Integer teachers;
    private Integer branches;
    private String currentErp;
    private String website;
    private String existingProblems;
    private CrmLeadSource leadSource;
    private Long assignedEmployeeId;
    private String assignedEmployeeName;
    private String priority;
    private LocalDateTime expectedClosingDate;
    private Integer leadRating;
    private String notes;
    private CrmPipelineStage pipelineStage;
    private String status;
    private Long convertedSchoolId;
    private String lostReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private List<CrmFollowUpDto> followUps = new ArrayList<>();
    private List<CrmDemoDto> demos = new ArrayList<>();
    private List<CrmQuotationDto> quotations = new ArrayList<>();
    private List<CrmActivityLogDto> activityLogs = new ArrayList<>();
}
