package com.school.erp.dto.exam;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class GenerateAdmitCardRequest {

    @NotNull(message = "Exam setup ID is required")
    private Long examSetupId;

    private Long classId;

    private Long sectionId;

    private List<Long> studentIds;

    private String templateName = "STANDARD";
}
