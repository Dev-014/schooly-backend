package com.school.erp.dto.hr;

import lombok.Data;

@Data
public class EmployeePerformanceDTO {
    private Long id;
    private Long employeeId;
    private String employeeName;
    private String reviewCycle;
    private Integer rating;
    private Long reviewerId;
    private String reviewerName;
    private String comments;
    private String goals;
}
