package com.school.erp.dto.exam;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TermPerformanceItem {
    private String termName; // e.g. "Term 1 (Sept - Nov)"
    private BigDecimal avgPercent; // e.g. 72.0
}
