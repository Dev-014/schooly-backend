package com.school.erp.dto.auth;

import lombok.Builder;
import java.time.LocalDateTime;

@Builder
public record UserAssignmentResponse(
        Long id,
        Long schoolId,
        Long userId,
        Long academicSessionId,
        String assignmentType,
        Long classId,
        Long sectionId,
        Long subjectId,
        Long departmentId,
        LocalDateTime effectiveFrom,
        LocalDateTime effectiveTo,
        boolean isActive
) {}
