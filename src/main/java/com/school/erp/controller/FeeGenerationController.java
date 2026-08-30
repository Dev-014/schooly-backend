package com.school.erp.controller;

import com.school.erp.api.ApiResponse;
import com.school.erp.dto.payment.FeeGenerationPreviewRequest;
import com.school.erp.dto.payment.FeeGenerationPreviewResponse;
import com.school.erp.dto.payment.FeeGenerationConfirmRequest;
import com.school.erp.service.FeeDueService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/finance/fee-generation")
public class FeeGenerationController {

    private final FeeDueService feeDueService;

    public FeeGenerationController(FeeDueService feeDueService) {
        this.feeDueService = feeDueService;
    }

    @PostMapping("/preview")
    public ResponseEntity<ApiResponse<FeeGenerationPreviewResponse>> previewBatchGeneration(
            @RequestParam Long schoolId,
            @RequestBody FeeGenerationPreviewRequest request) {
        FeeGenerationPreviewResponse response = feeDueService.previewBatchGeneration(schoolId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Preview successful"));
    }

    @PostMapping("/confirm")
    public ResponseEntity<ApiResponse<Void>> confirmBatchGeneration(
            @RequestParam Long schoolId,
            @RequestBody FeeGenerationConfirmRequest request) {
        // Hardcoding 'SYSTEM' as generator for now, can be extracted from principal/token later
        feeDueService.confirmBatchGeneration(schoolId, request, "SYSTEM");
        return ResponseEntity.ok(ApiResponse.success(null, "Fees generated successfully for eligible students."));
    }
}
