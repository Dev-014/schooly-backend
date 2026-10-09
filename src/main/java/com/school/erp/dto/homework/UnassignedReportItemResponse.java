package com.school.erp.dto.homework;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UnassignedReportItemResponse {

    private Long id;
    private Long classId;
    private String className;
    private Long sectionId;
    private String sectionName;
    private Long subjectId;
    private String subjectName;
    private String subjectCode;
    private String faculty;
    private String status; // "Urgent" or "Standard"
    private LocalDate lastAssignedDate;
}
