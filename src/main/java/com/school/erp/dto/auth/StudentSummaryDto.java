package com.school.erp.dto.auth;

import com.fasterxml.jackson.annotation.JsonProperty;

public record StudentSummaryDto(
        Long id,
        String name,
        String admissionNo,
        String className,
        Long schoolId,
        String schoolName
) {
    @JsonProperty("studentId")
    public Long getStudentId() {
        return id;
    }

    @JsonProperty("studentName")
    public String getStudentName() {
        return name;
    }

    @JsonProperty("admissionNumber")
    public String getAdmissionNumber() {
        return admissionNo;
    }

    @JsonProperty("grade")
    public String getGrade() {
        return className;
    }
}
