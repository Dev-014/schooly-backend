package com.school.erp.dto.hr;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Data
public class StaffLeaveRequest {
    private Long staffId;

    @NotNull(message = "Leave Type ID is required")
    private Long leaveTypeId;

    @NotNull(message = "Start Date is required")
    @JsonAlias({"fromDate", "startDate"})
    private LocalDate startDate;

    @NotNull(message = "End Date is required")
    @JsonAlias({"toDate", "endDate"})
    private LocalDate endDate;

    private String reason;

    @JsonProperty("fromDate")
    public void setFromDate(LocalDate fromDate) {
        if (this.startDate == null) {
            this.startDate = fromDate;
        }
    }

    @JsonProperty("toDate")
    public void setToDate(LocalDate toDate) {
        if (this.endDate == null) {
            this.endDate = toDate;
        }
    }
}

