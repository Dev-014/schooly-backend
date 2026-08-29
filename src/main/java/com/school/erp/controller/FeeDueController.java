package com.school.erp.controller;

import com.school.erp.api.ApiResponse;
import com.school.erp.dto.payment.FeeDueResponse;
import com.school.erp.service.FeeDueService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/finance/students/{studentId}/dues")
public class FeeDueController {

    private final FeeDueService feeDueService;

    public FeeDueController(FeeDueService feeDueService) {
        this.feeDueService = feeDueService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<FeeDueResponse>>> getStudentDues(
            @PathVariable Long studentId,
            @RequestParam Long schoolId,
            @RequestParam(required = false) List<String> statuses) {
        return ResponseEntity.ok(
                ApiResponse.success(feeDueService.getStudentDues(studentId, schoolId, statuses), "Dues fetched successfully")
        );
    }

    @PatchMapping("/{dueId}/discount")
    public ResponseEntity<ApiResponse<FeeDueResponse>> applyDiscount(
            @PathVariable Long studentId,
            @PathVariable Long dueId,
            @RequestParam Long schoolId,
            @jakarta.validation.Valid @RequestBody com.school.erp.dto.payment.ApplyDiscountRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success(feeDueService.applyDiscount(studentId, dueId, schoolId, request), "Discount applied successfully")
        );
    }

    @PostMapping("/ad-hoc")
    public ResponseEntity<ApiResponse<FeeDueResponse>> assignAdHocFee(
            @PathVariable Long studentId,
            @RequestParam Long schoolId,
            @jakarta.validation.Valid @RequestBody com.school.erp.dto.payment.AdHocFeeRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success(feeDueService.assignAdHocFee(
                        studentId, 
                        schoolId, 
                        request.feeCategoryId(), 
                        request.amount(), 
                        request.title(), 
                        request.dueDate()
                ), "Ad-Hoc fee assigned successfully")
        );
    }
    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<List<FeeDueResponse>>> generateBaseDues(
            @PathVariable Long studentId,
            @RequestParam Long schoolId) {
        return ResponseEntity.ok(
                ApiResponse.success(feeDueService.generateBaseDuesForStudent(studentId, schoolId), "Base dues generated successfully")
        );
    }
}
