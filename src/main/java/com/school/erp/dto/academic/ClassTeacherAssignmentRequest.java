package com.school.erp.dto.academic;

import jakarta.validation.constraints.NotNull;

public record ClassTeacherAssignmentRequest(
        Long schoolId, // Can be null if extracted from auth
        @NotNull(message = "Staff ID is required") Long staffId,
        @NotNull(message = "Class ID is required") Long classId,
        @NotNull(message = "Section ID is required") Long sectionId,
        @NotNull(message = "Academic Year ID is required") Long academicYearId,
        String status // Optional, defaults to ACTIVE
) {}
