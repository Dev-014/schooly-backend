package com.school.erp.dto.academic;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TimetableEntryRequest(
        Long schoolId,
        @NotNull Long classId,
        Long sectionId,
        @NotNull Long academicYearId,
        @NotBlank String dayOfWeek,
        @NotNull Long periodId,
        Long subjectId,
        Long teacherId,
        String roomNumber
) {}
