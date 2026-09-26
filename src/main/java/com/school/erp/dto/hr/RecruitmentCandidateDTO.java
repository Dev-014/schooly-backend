package com.school.erp.dto.hr;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecruitmentCandidateDTO {
    private Long id;
    private Long schoolId;
    private String name;
    private String phone;
    private String email;
    private LocalDate dateOfBirth;
    private String applyingFor;
    private BigDecimal expectedSalary;
    private String maritalStatus;
    private String workExperience;
    private LocalDateTime interviewDate;
    private LocalDateTime submissionDate;
    private String status;
    private String description;
    private String documentUrl;
}
