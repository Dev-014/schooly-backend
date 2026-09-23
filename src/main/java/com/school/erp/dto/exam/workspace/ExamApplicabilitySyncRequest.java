package com.school.erp.dto.exam.workspace;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class ExamApplicabilitySyncRequest {
    @Data
    public static class ClassSectionPair {
        @NotNull(message = "Class ID is required")
        private Long classId;
        private Long sectionId;
    }

    @NotNull(message = "Applicabilities list cannot be null")
    private List<ClassSectionPair> applicabilities;
}
