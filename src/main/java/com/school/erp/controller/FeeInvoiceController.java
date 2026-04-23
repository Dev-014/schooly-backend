package com.school.erp.controller;

import com.school.erp.api.ApiResponse;
import com.school.erp.dto.feeinvoice.FeeInvoiceRequest;
import com.school.erp.dto.feeinvoice.FeeInvoiceResponse;
import com.school.erp.service.FeeInvoiceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/fee-invoices")
public class FeeInvoiceController {

    private final FeeInvoiceService feeInvoiceService;

    public FeeInvoiceController(FeeInvoiceService feeInvoiceService) {
        this.feeInvoiceService = feeInvoiceService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<FeeInvoiceResponse>>> getAllInvoices(
            @RequestParam(required = false) Long schoolId,
            @RequestParam(required = false) Long studentId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                feeInvoiceService.getAllInvoices(schoolId, studentId),
                "Fee invoices fetched successfully"
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FeeInvoiceResponse>> getInvoiceById(
            @PathVariable Long id,
            @RequestParam(required = false) Long schoolId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                feeInvoiceService.getInvoiceById(id, schoolId),
                "Fee invoice fetched successfully"
        ));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<FeeInvoiceResponse>> createInvoice(@Valid @RequestBody FeeInvoiceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                feeInvoiceService.createInvoice(request),
                "Fee invoice created successfully"
        ));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<FeeInvoiceResponse>> updateInvoice(
            @PathVariable Long id,
            @RequestParam(required = false) Long schoolId,
            @Valid @RequestBody FeeInvoiceRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                feeInvoiceService.updateInvoice(id, schoolId, request),
                "Fee invoice updated successfully"
        ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteInvoice(
            @PathVariable Long id,
            @RequestParam(required = false) Long schoolId
    ) {
        feeInvoiceService.deleteInvoice(id, schoolId);
        return ResponseEntity.ok(ApiResponse.success(null, "Fee invoice deleted successfully"));
    }
}
