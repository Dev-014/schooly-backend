package com.school.erp.dto.superadmin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OnboardingMatrixDto {
    private Long schoolId;
    private String schoolName;
    private String basicInfoStatus;
    private String agreementStatus;
    private String paymentStatus;
    private String configStatus;
    private String trainingStatus;
    private String goLiveStatus;
}
