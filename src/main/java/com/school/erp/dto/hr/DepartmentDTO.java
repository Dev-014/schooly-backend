package com.school.erp.dto.hr;

import lombok.Data;

@Data
public class DepartmentDTO {
    private Long id;
    private String name;
    private Long headEmployeeId;
    private String headEmployeeName;
    private String description;
}
