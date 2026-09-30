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
public class EntranceExamResponse {

    private Long id;
    private Long schoolId;
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
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
