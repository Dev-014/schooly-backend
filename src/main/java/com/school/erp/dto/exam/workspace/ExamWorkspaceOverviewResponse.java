package com.school.erp.dto.exam.workspace;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamWorkspaceOverviewResponse {
    private Long id;
    private String name;
    private Long termId;
    private String termName;
    private String examType;
    private String status;
    private String evaluationType;

    // Summary statistics for the tabs
    private int subjectCount;
    private int classCount;
    private int studentCount;
    private int scheduleCount;
}
