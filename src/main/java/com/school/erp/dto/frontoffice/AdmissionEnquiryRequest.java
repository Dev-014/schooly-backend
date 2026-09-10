package com.school.erp.dto.frontoffice;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdmissionEnquiryRequest {

    private String enquiryNumber;

    @NotBlank(message = "Student name is required")
    private String studentName;

    private String parentName;

    private String phone;

    private String email;

    @NotNull(message = "Enquiry date is required")
    private LocalDate enquiryDate;

    private LocalDate nextFollowUpDate;

    private String source;

    private String status; // ACTIVE, CONVERTED, PASSIVE, LOST

    private Long classId;

    private Integer numberOfChildren;

    private Long assignedToStaffId;

    private String detailedNotes;
}
