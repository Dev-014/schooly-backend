package com.school.erp.dto.student;

import jakarta.validation.constraints.NotNull;

public class FamilyRequest {
    
    @NotNull
    private Long schoolId;
    
    private String familyCode;
    
    private String status;

    public FamilyRequest() {
    }

    public FamilyRequest(Long schoolId, String familyCode, String status) {
        this.schoolId = schoolId;
        this.familyCode = familyCode;
        this.status = status;
    }

    public Long getSchoolId() {
        return schoolId;
    }

    public void setSchoolId(Long schoolId) {
        this.schoolId = schoolId;
    }

    public String getFamilyCode() {
        return familyCode;
    }

    public void setFamilyCode(String familyCode) {
        this.familyCode = familyCode;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
