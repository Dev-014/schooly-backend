package com.school.erp.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeacherClassResponse {
    private Long id; // Assignment ID or Class ID
    private Long classId;
    private String className;
    private Long sectionId;
    private String sectionName;
}
