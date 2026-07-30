package com.school.erp.dto;

import lombok.Data;

@Data
public class SuperAdminEmployeeDTO {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String status;
    private String joinedAt;
    private Integer activeTickets;
}
