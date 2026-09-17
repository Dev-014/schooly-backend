package com.school.erp.dto.exam;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
public class AdmitCardPreviewResponse {
    private String schoolName;
    private String schoolCode;
    private String examName;
    private String termName;
    private String candidateName;
    private String rollNumber;
    private String admissionNo;
    private String gradeAndSection;
    private String verificationStatus;
    private String cardNumber;
    private String templateName;
    private List<AdmitCardScheduleItem> schedule;

    @Data
    @Builder
    public static class AdmitCardScheduleItem {
        private String subjectName;
        private String subjectCode;
        private LocalDate date;
        private LocalTime startTime;
        private LocalTime endTime;
        private String formattedTime;
        private String roomNumber;
    }
}
