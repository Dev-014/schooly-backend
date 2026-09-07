package com.school.erp.dto.auth;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record UserAssignmentRequest(
        @NotNull String assignmentType,
        @NotNull Long academicSessionId,
        Long classId,
        Long sectionId,
        Long subjectId,
        Long departmentId
) {}
