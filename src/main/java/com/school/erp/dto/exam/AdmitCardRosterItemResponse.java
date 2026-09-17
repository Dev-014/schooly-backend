package com.school.erp.dto.exam;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AdmitCardRosterItemResponse {
    private Long id;
    private Long studentId;
    private String studentName;
    private String rollNumber;
    private String admissionNo;
    private Long classId;
    private String className;
    private Long sectionId;
    private String sectionName;
    private String status; // PENDING, GENERATED, RELEASED
    private String cardNumber;
    private LocalDateTime generatedAt;
    private LocalDateTime releasedAt;
}
