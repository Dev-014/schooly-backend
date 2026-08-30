package com.school.erp.repository;

import java.math.BigDecimal;

public interface StudentFeeAggregation {
    Long getStudentId();
    BigDecimal getTotalFees();
    BigDecimal getAmountPaid();
}
