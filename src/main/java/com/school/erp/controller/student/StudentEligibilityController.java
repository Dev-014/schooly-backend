package com.school.erp.controller;

import com.school.erp.api.ApiResponse;
import com.school.erp.service.finance.StudentEligibilityService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/finance/students/{studentId}/eligibility")
public class StudentEligibilityController {

    private final StudentEligibilityService eligibilityService;

    public StudentEligibilityController(StudentEligibilityService eligibilityService) {
        this.eligibilityService = eligibilityService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<StudentEligibilityService.EligibilityStatus>> getEligibility(
            @PathVariable Long studentId,
            @RequestParam Long schoolId) {
        StudentEligibilityService.EligibilityStatus status = eligibilityService.getEligibility(studentId, schoolId);
        return ResponseEntity.ok(ApiResponse.success(status, "Eligibility fetched successfully"));
    }
}
