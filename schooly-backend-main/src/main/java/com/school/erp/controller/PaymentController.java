package com.school.erp.controller;

import com.school.erp.api.ApiResponse;
import com.school.erp.dto.payment.PaymentRequest;
import com.school.erp.dto.payment.PaymentResponse;
import com.school.erp.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getPayments(
            @RequestParam(required = false) Long schoolId,
            @RequestParam(required = false) Long invoiceId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                paymentService.getAllPayments(schoolId, invoiceId),
                "Payments fetched successfully"
        ));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PaymentResponse>> createPayment(@Valid @RequestBody PaymentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                paymentService.createPayment(request),
                "Payment created successfully"
        ));
    }
}
