package com.school.erp.dto.auth;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record UserAssignmentRequest(
        @NotNull String assignmentType,
        @NotNull @JsonAlias({"academicYearId", "academic_year_id"}) Long academicSessionId,
        Long classId,
        Long sectionId,
        Long subjectId,
        Long departmentId
) {}
