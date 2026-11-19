package com.school.erp.service.finance;

import com.school.erp.dto.payment.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FeePaymentService {
    PaymentResponse processPayment(FeePaymentRequest request);
    Page<PaymentResponse> getPaymentsBySchool(Long schoolId, String search, Pageable pageable);
    Page<PaymentResponse> getPaymentsByStudent(Long studentId, Long schoolId, Pageable pageable);

    // Track A: Online Gateway (Razorpay Route)
    GatewayOrderResponse createOnlineOrder(GatewayOrderCreateRequest request);
    GatewayVerificationResponse verifyOnlinePayment(GatewayPaymentVerifyRequest request);

    // Track A: Offline Hybrid Verification Queue with Document Proof
    PaymentResponse submitOfflinePayment(OfflinePaymentSubmissionRequest request, Long submittedByUserId);
    Page<PaymentResponse> getPendingVerifications(Long schoolId, Pageable pageable);
    PaymentResponse verifyOfflinePayment(Long paymentId, OfflinePaymentVerificationRequest request, Long verifiedByUserId);

    // Unified Webhook Processing
    void processWebhookEvent(String payload, String signature);
}
