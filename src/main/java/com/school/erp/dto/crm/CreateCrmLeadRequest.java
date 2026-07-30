package com.school.erp.dto.crm;

import com.school.erp.entity.crm.CrmLeadSource;
import lombok.Data;

@Data
public class CreateCrmLeadRequest {
    private String schoolName;
    private String principalName;
    private String city;
    private String board;
    private String mobile;
    private String alternativeMobile;
    private String email;
    private String address;
    private String state;
    private String pinCode;
    private Integer approxStudentStrength;
    private Integer teachers;
    private Integer branches;
    private String currentErp;
    private String website;
    private String existingProblems;
    private CrmLeadSource leadSource;
}
