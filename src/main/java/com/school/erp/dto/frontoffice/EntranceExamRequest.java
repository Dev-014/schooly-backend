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
public class EntranceExamRequest {

    @NotBlank(message = "Candidate name is required")
    private String candidateName;

    private String mobileNumber;
    private String parentName;
    private String gender;
    private String className;
    private String examName;
    private String centerName;
    private LocalDate examDate;
    private String examTime;
    private String status;
    private Double score;
    private String notes;
}
