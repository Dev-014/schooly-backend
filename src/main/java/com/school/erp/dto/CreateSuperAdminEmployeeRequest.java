package com.school.erp.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateSuperAdminEmployeeRequest {
    @NotBlank
    private String name;
    
    @NotBlank
    @Email
    private String email;
    
    @NotBlank
    private String phone;
    
    @NotBlank
    private String password;
}
