package com.school.erp.dto;

import lombok.Data;

@Data
public class DepartmentDTO {
    private Long id;
    private String name;
    private Long headEmployeeId;
    private String headEmployeeName;
    private String description;
}
