package com.school.erp.dto.academic;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public record BulkTimetableRequest(
        Long schoolId,
        @NotNull Long classId,
        Long sectionId,
        @NotNull Long academicYearId,
        @NotNull List<TimetableEntryRequest> entries
) {}
