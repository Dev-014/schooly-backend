package com.school.erp.dto.frontoffice;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GatePassStatsResponse {

    private long issuedToday;
    private long approvedToday;
}
