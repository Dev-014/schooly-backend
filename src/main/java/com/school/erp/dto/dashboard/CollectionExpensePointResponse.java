package com.school.erp.dto.dashboard;

import java.math.BigDecimal;

public record CollectionExpensePointResponse(
        String month,
        BigDecimal collected,
        BigDecimal expense
) {
}
