package com.school.erp.dto.student;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FamilyResponse {
    
    private Long id;
    private Long schoolId;
    private String familyCode;
    private String status;
    private String createdAt;
    private String updatedAt;
    private List<FamilyMemberResponse> members;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FamilyMemberResponse {
        private Long id;
        private String name;
        private String admissionNo;
        private String gender;
        private String status;
    }
}
