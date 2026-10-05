package com.school.erp.dto.library;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LibraryReturnRequest {

    private LocalDate returnDate;
    private BigDecimal fineAmount;
    private Boolean finePaid;
    private String remarks;
}
