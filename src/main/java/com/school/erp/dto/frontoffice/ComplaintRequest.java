package com.school.erp.dto.frontoffice;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComplaintRequest {

    @NotBlank(message = "Complainant name is required")
    private String complainantName;

    @Pattern(regexp = "^$|^\\d{10}$", message = "Mobile number must be exactly 10 digits")
    private String mobileNumber;

    private String complaintCategory;

    private Long targetStudentId;

    private Long targetStaffId;

    private String locationOrAsset;

    @NotBlank(message = "Complaint type is required")
    private String complaintType;

    private String description;

    private String assignedBy;

    private String actionTaken;

    private String status;

    private String complaintCode;

    private Long createdByUserId;

    private String createdByRole;

    private String sourceScope;

    private String priority;

    private LocalDate complaintDate;
}
