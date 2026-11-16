package com.school.erp.dto.accounting;

import java.math.BigDecimal;

public record AccountingSummaryResponse(
        BigDecimal total,
        BigDecimal cash,
        BigDecimal bank,
        BigDecimal online
) {
}
