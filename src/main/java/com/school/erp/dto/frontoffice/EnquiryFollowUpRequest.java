package com.school.erp.dto.frontoffice;

import jakarta.validation.constraints.NotBlank;
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
public class EnquiryFollowUpRequest {

    @NotBlank(message = "Action type is required")
    private String actionType; // Inbound Call, Outbound Call, Document Upload, Campus Tour, Meeting, Email

    private String notes;

    private LocalDateTime followUpDate;

    private Long recordedByStaffId;

    private LocalDate nextFollowUpDate;

    private String updatedStatus;
}
