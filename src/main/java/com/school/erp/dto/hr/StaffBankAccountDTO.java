package com.school.erp.dto.hr;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffBankAccountDTO {
    private Long id;
    private Long staffId;
    private String accountHolderName;
    private String accountNumber;
    private String bankName;
    private String ifscCode;
    private String branchName;
}
