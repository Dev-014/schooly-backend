package com.school.erp.controller;

import com.school.erp.api.ApiResponse;
import com.school.erp.dto.finance.FeeInstallmentRequest;
import com.school.erp.dto.finance.FeeInstallmentResponse;
import com.school.erp.service.finance.FeeInstallmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/finance/installments")
@RequiredArgsConstructor
public class FeeInstallmentController {

    private final FeeInstallmentService feeInstallmentService;

    @PostMapping
    public ResponseEntity<ApiResponse<FeeInstallmentResponse>> createInstallment(
            @RequestParam Long schoolId,
            @Valid @RequestBody FeeInstallmentRequest request) {
        FeeInstallmentResponse response = feeInstallmentService.createInstallment(schoolId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Fee Installment created successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<FeeInstallmentResponse>>> getInstallments(
            @RequestParam Long schoolId,
            @RequestParam(required = false) Long academicYearId) {
        List<FeeInstallmentResponse> response = feeInstallmentService.getInstallments(schoolId, academicYearId);
        return ResponseEntity.ok(ApiResponse.success(response, "Fee Installments fetched successfully"));
    }
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<FeeInstallmentResponse>> updateInstallment(
            @PathVariable Long id,
            @RequestParam Long schoolId,
            @Valid @RequestBody FeeInstallmentRequest request) {
        FeeInstallmentResponse response = feeInstallmentService.updateInstallment(id, schoolId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Fee Installment updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteInstallment(
            @PathVariable Long id,
            @RequestParam Long schoolId) {
        feeInstallmentService.deleteInstallment(id, schoolId);
        return ResponseEntity.ok(ApiResponse.success(null, "Fee Installment deleted successfully"));
    }
}
