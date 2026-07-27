package com.school.erp.dto.academic;

import jakarta.validation.constraints.NotNull;

public record StudentSubjectEnrollmentRequest(
        Long schoolId,
        @NotNull Long studentId,
        @NotNull Long subjectId,
        @NotNull Long academicYearId,
        String enrollmentType  // "CORE" or "ELECTIVE" — defaults to "ELECTIVE"
) {}
