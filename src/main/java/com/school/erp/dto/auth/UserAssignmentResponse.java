package com.school.erp.dto.auth;

import lombok.Builder;
import java.time.LocalDateTime;

@Builder
public record UserAssignmentResponse(
        Long id,
        Long schoolId,
        Long userId,
        Long academicSessionId,
        String sessionName,
        String assignmentType,
        Long classId,
        String className,
        Long sectionId,
        String sectionName,
        Long subjectId,
        String subjectName,
        String subjectCode,
        Long departmentId,
        String departmentName,
        LocalDateTime effectiveFrom,
        LocalDateTime effectiveTo,
        boolean isActive
) {}
