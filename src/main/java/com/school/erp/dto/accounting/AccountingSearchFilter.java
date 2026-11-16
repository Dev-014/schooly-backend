package com.school.erp.dto.accounting;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class AccountingSearchFilter {
    private Long academicYearId;
    private Long headId;
    private String accountType;
    private LocalDate startDate;
    private LocalDate endDate;
    private String paymentMode;

    @Min(1)
    @Max(12)
    private Integer month;

    @Min(1900)
    @Max(2200)
    private Integer year;
}
