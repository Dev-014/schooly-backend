package com.school.erp.dto.superadmin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SchoolDto {
    private Long id;
    private String name;
    private String code;
    private String address;
    private String contactEmail;
    private String contactPhone;
    private String status;
}
