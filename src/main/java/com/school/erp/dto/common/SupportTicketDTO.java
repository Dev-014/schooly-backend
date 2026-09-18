package com.school.erp.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class SupportTicketDTO {
    private Long id;
    private String ticketCode;
    private Long schoolId;
    private String schoolName;
    private Long creatorUserId;
    private String creatorName;
    private String portalSource;
    private Long categoryId;
    private String categoryName;
    private String priority;
    private String subject;
    private String description;
    private String attachmentUrl;
    private String status;
    private Long assignedEmployeeId;
    private String assignedEmployeeName;
    private LocalDateTime createdAt;
    private LocalDateTime closedAt;
}
