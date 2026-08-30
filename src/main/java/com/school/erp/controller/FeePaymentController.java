package com.school.erp.controller;

import com.school.erp.api.ApiResponse;
import com.school.erp.dto.payment.FeePaymentRequest;
import com.school.erp.dto.payment.PaymentResponse;
import com.school.erp.service.FeePaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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
}
