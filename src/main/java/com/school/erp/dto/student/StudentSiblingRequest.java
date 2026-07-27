package com.school.erp.dto.student;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record StudentSiblingRequest(
        @NotNull(message = "schoolId is required") Long schoolId,
        @NotNull(message = "primaryStudentId is required") Long primaryStudentId,
        @NotNull(message = "siblingStudentId is required") Long siblingStudentId,
        @NotBlank(message = "relationship is required") String relationship
) {
}
