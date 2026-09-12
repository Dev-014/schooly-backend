package com.school.erp.dto.frontoffice;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdmissionEnquiryStatsResponse {

    private long totalEnquiries;
    private long activeEnquiries;
    private long convertedEnquiries;
    private long passiveEnquiries;
    private long lostEnquiries;
    private long followUpsDueToday;
    private long followUpsOverdue;
}
