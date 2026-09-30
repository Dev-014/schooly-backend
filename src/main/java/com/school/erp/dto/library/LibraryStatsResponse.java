package com.school.erp.dto.library;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LibraryStatsResponse {

    private long totalBooks;
    private long totalCopies;
    private long availableCopies;
    private long issuedBooks;
    private long overdueBooks;
    private long totalMembers;
    private BigDecimal finesCollected;
    private BigDecimal finesPending;
}
