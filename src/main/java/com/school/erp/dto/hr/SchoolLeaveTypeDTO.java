package com.school.erp.dto.hr;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SchoolLeaveTypeDTO {
    private Long id;
    private Long schoolId;
    private String name;
    private Integer daysAllowed;
    private Boolean isPaid;
    private String applicableRoles;
}
