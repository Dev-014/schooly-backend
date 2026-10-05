package com.school.erp.dto.hr;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffTaskDTO {
    private Long id;
    private Long schoolId;
    private Long staffId;
    private String staffName;
    private String title;
    private String description;
    private LocalDate startDate;
    private LocalDate dueDate;
    private String priority;
    private String status;
}
