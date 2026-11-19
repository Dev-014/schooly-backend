package com.school.erp.controller.finance;

import com.school.erp.api.ApiResponse;
import com.school.erp.dto.payment.*;
import com.school.erp.security.AuthContextHolder;
import com.school.erp.security.AuthenticatedUser;
import com.school.erp.service.finance.FeePaymentService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/finance/payments")
public class FeePaymentController {

    private final FeePaymentService feePaymentService;

    public FeePaymentController(FeePaymentService feePaymentService) {
        this.feePaymentService = feePaymentService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PaymentResponse>> processPayment(@Valid @RequestBody FeePaymentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success(feePaymentService.processPayment(request), "Payment processed successfully")
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<PaymentResponse>>> getPayments(
            @RequestParam Long schoolId,
            @RequestParam(required = false) String search,
            Pageable pageable) {
        return ResponseEntity.ok(
                ApiResponse.success(feePaymentService.getPaymentsBySchool(schoolId, search, pageable), "Payments fetched successfully")
        );
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<ApiResponse<Page<PaymentResponse>>> getStudentPayments(
            @PathVariable Long studentId,
            @RequestParam Long schoolId,
            Pageable pageable) {
        return ResponseEntity.ok(
                ApiResponse.success(feePaymentService.getPaymentsByStudent(studentId, schoolId, pageable), "Student payments fetched successfully")
        );
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Track A: Online Payment Gateway Endpoints (Razorpay Route)
    // ─────────────────────────────────────────────────────────────────────────
    @PostMapping("/create-order")
    public ResponseEntity<ApiResponse<GatewayOrderResponse>> createOnlineOrder(
            @Valid @RequestBody GatewayOrderCreateRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success(feePaymentService.createOnlineOrder(request), "Payment order created successfully")
        );
    }

    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<GatewayVerificationResponse>> verifyOnlinePayment(
            @Valid @RequestBody GatewayPaymentVerifyRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success(feePaymentService.verifyOnlinePayment(request), "Payment verification processed")
        );
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Track A: Offline Payment Submission & Accountant Verification Queue
    // ─────────────────────────────────────────────────────────────────────────
    @PostMapping("/offline")
    public ResponseEntity<ApiResponse<PaymentResponse>> submitOfflinePayment(
            @Valid @RequestBody OfflinePaymentSubmissionRequest request) {
        AuthenticatedUser user = AuthContextHolder.get();
        Long userId = user != null ? user.userId() : null;

        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success(feePaymentService.submitOfflinePayment(request, userId), "Offline payment submitted successfully")
        );
    }

    @GetMapping("/pending-verifications")
    public ResponseEntity<ApiResponse<Page<PaymentResponse>>> getPendingVerifications(
            @RequestParam Long schoolId,
            Pageable pageable) {
        return ResponseEntity.ok(
                ApiResponse.success(feePaymentService.getPendingVerifications(schoolId, pageable), "Pending verifications fetched successfully")
        );
    }

    @PatchMapping("/{paymentId}/verify-offline")
    public ResponseEntity<ApiResponse<PaymentResponse>> verifyOfflinePayment(
            @PathVariable Long paymentId,
            @Valid @RequestBody OfflinePaymentVerificationRequest request) {
        AuthenticatedUser user = AuthContextHolder.get();
        Long userId = user != null ? user.userId() : null;

        return ResponseEntity.ok(
                ApiResponse.success(feePaymentService.verifyOfflinePayment(paymentId, request, userId), "Offline payment verification completed")
        );
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Webhook Endpoint (also mirrored at /api/v1/webhooks/razorpay)
    // ─────────────────────────────────────────────────────────────────────────
    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(
            @RequestBody String payload,
            @RequestHeader(value = "X-Razorpay-Signature", required = false) String signature) {
        feePaymentService.processWebhookEvent(payload, signature);
        return ResponseEntity.ok("OK");
    }
}
