package com.school.erp.service;

import com.school.erp.dto.payment.FeePaymentRequest;
import com.school.erp.dto.payment.PaymentResponse;

public interface FeePaymentService {
    PaymentResponse processPayment(FeePaymentRequest request);
}
