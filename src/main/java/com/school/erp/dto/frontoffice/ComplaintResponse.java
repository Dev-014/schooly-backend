package com.school.erp.dto.frontoffice;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComplaintResponse {

    private Long id;
    private Long schoolId;
    private String complaintCode;
    private String complainantName;
    private String mobileNumber;
    private String complaintType;
    private String description;
    private String assignedBy;
    private String actionTaken;
    private String status;
    private Long createdByUserId;
    private String createdByRole;
    private String sourceScope;
    private String priority;
    private Boolean isOwn;
    private LocalDate complaintDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
