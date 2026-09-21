package com.school.erp.repository.finance;

import java.math.BigDecimal;

public interface StudentFeeAggregation {
    Long getStudentId();
    BigDecimal getTotalFees();
    BigDecimal getAmountPaid();
}
