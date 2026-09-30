package com.school.erp.dto.frontoffice;

import jakarta.validation.constraints.NotBlank;
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

    private String mobileNumber;

    @NotBlank(message = "Complaint type is required")
    private String complaintType;

    private String description;

    private String assignedBy;

    private String actionTaken;

    private String status;

    private LocalDate complaintDate;
}
