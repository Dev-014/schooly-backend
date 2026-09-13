package com.school.erp.dto.exam;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class BulkExamMarksRequest {

    @NotEmpty(message = "Marks list cannot be empty")
    @Valid
    private List<ExamMarkEntryRequest> marks;
}
