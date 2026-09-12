package com.school.erp.dto.frontoffice;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdmissionEnquiryResponse {

    private Long id;
    private Long schoolId;
    private String enquiryNumber;
    private String studentName;
    private String parentName;
    private String phone;
    private String email;
    private LocalDate enquiryDate;
    private LocalDate nextFollowUpDate;
    private String followUpTag; // TODAY, OVERDUE, UPCOMING, etc.
    private String source;
    private String status;
    private Long classId;
    private String className;
    private Integer numberOfChildren;
    private Long assignedToStaffId;
    private String assignedToStaffName;
    private String detailedNotes;
    private List<EnquiryFollowUpResponse> followUps;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
