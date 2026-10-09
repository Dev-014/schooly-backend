package com.school.erp.dto.homework;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.school.erp.entity.homework.AssignmentStatus;
import com.school.erp.entity.homework.AssignmentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HomeworkAssignmentRequest {

    @Builder.Default
    private AssignmentType assignmentType = AssignmentType.HOMEWORK;

    @NotNull(message = "classId is required")
    private Long classId;

    private Long sectionId;

    @NotNull(message = "subjectId is required")
    private Long subjectId;

    private Long teacherId;

    private Long academicYearId;

    @NotBlank(message = "title is required")
    private String title;

    private String description;

    private String instructions;

    @NotNull(message = "assignedDate is required")
    @JsonAlias({"assignmentDate", "assigned_date"})
    private LocalDate assignedDate;

    @NotNull(message = "dueDate is required")
    @JsonAlias({"due_date"})
    private LocalDate dueDate;

    @Builder.Default
    @JsonAlias({"totalMarks", "total_marks", "max_marks"})
    private BigDecimal maxMarks = BigDecimal.valueOf(100.00);

    private String attachmentUrl;

    private String attachmentName;

    @Builder.Default
    private AssignmentStatus status = AssignmentStatus.PUBLISHED;

    public void setAssignmentDate(LocalDate assignmentDate) {
        if (this.assignedDate == null) {
            this.assignedDate = assignmentDate;
        }
    }

    public LocalDate getAssignmentDate() {
        return this.assignedDate;
    }

    public void setTotalMarks(BigDecimal totalMarks) {
        if (totalMarks != null) {
            this.maxMarks = totalMarks;
        }
    }

    public BigDecimal getTotalMarks() {
        return this.maxMarks;
    }
}
