package com.school.erp.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class TicketHistoryDTO {
    private Long id;
    private Long ticketId;
    private String oldStatus;
    private String newStatus;
    private Long employeeId;
    private String employeeName;
    private String remark;
    private LocalDateTime createdAt;
}
