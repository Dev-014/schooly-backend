package com.school.erp.dto.exam.workspace;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class ExamSubjectConfigUpdateRequest {
    @NotNull(message = "Max marks is required")
    @Min(value = 0, message = "Max marks cannot be negative")
    private BigDecimal maxMarks;

    @NotNull(message = "Passing marks is required")
    @Min(value = 0, message = "Passing marks cannot be negative")
    private BigDecimal passingMarks;

    private BigDecimal theoryMarks;
    private BigDecimal practicalMarks;
    private BigDecimal internalMarks;
}
