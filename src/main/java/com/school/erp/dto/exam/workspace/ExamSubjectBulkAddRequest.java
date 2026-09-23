package com.school.erp.dto.exam.workspace;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class ExamSubjectBulkAddRequest {
    @NotEmpty(message = "Subject IDs list cannot be empty")
    private List<Long> subjectIds;
}
