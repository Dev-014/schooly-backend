package com.school.erp.dto.hr;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffLeaveDTO {
    private Long id;
    private Long schoolId;
    private Long staffId;
    private String staffName;
    private Long leaveTypeId;
    private String leaveTypeName;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal numberOfDays;
    private String reason;
    private String status;
    private Long approvedBy;

    @JsonProperty("fromDate")
    public LocalDate getFromDate() {
        return startDate;
    }

    @JsonProperty("toDate")
    public LocalDate getToDate() {
        return endDate;
    }
}

