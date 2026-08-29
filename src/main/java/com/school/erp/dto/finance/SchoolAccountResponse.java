package com.school.erp.dto.finance;

import lombok.Data;

@Data
public class SchoolAccountResponse {
    private Long id;
    private String accountName;
    private String accountType;
    private Boolean isActive;
}
