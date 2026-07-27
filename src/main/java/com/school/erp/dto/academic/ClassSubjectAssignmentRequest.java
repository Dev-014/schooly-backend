package com.school.erp.dto.academic;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public record ClassSubjectAssignmentRequest(
        Long schoolId,
        @NotNull Long classId,
        Long sectionId,
        @NotNull Long academicYearId,
        @NotNull List<SubjectEntry> subjects
) {
    public record SubjectEntry(
            @NotNull Long subjectId,
            String subjectType  // "CORE" or "ELECTIVE" — defaults to "CORE"
    ) {}
}
