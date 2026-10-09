package com.school.erp.dto.homework;

import com.school.erp.entity.homework.AssignmentType;
import com.school.erp.entity.homework.SubmissionStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HomeworkSubmissionResponse {

    private Long id;
    private Long schoolId;
    private Long assignmentId;
    private String assignmentTitle;
    private AssignmentType assignmentType;
    private Long studentId;
    private String studentName;
    private String studentAdmissionNo;
    private String studentRollNo;
    private String className;
    private String sectionName;
    private LocalDateTime submissionDate;
    private String submissionText;
    private String attachmentUrl;
    private String attachmentName;
    private SubmissionStatus status;
    private BigDecimal marksObtained;
    private BigDecimal maxMarks;
    private String remarks;
    private Long evaluatedBy;
    private String evaluatorName;
    private LocalDateTime evaluatedAt;
    private String rubricScores;

    public SubmissionStatus getSubmissionStatus() {
        return status;
    }
}
