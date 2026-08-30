package com.school.erp.service;

import com.school.erp.dto.payment.FeePaymentRequest;
import com.school.erp.dto.payment.PaymentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FeePaymentService {
    PaymentResponse processPayment(FeePaymentRequest request);
    Page<PaymentResponse> getPaymentsBySchool(Long schoolId, String search, Pageable pageable);
}
