package com.school.erp.dto.homework;

import com.school.erp.entity.homework.AssignmentStatus;
import com.school.erp.entity.homework.AssignmentType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HomeworkAssignmentResponse {

    private Long id;
    private Long schoolId;
    private AssignmentType assignmentType;
    private Long classId;
    private String className;
    private Long sectionId;
    private String sectionName;
    private Long subjectId;
    private String subjectName;
    private String subjectCode;
    private Long teacherId;
    private String teacherName;
    private Long academicYearId;
    private String title;
    private String description;
    private String instructions;
    private LocalDate assignedDate;
    private LocalDate dueDate;
    private BigDecimal maxMarks;
    private String attachmentUrl;
    private String attachmentName;
    private AssignmentStatus status;
    private int totalStudentsCount;
    private int submittedCount;
    private int evaluatedCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public LocalDate getAssignmentDate() {
        return assignedDate;
    }

    public BigDecimal getTotalMarks() {
        return maxMarks;
    }
}
