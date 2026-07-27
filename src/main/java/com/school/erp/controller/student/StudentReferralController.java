package com.school.erp.controller.student;

import com.school.erp.api.ApiResponse;
import com.school.erp.dto.student.StudentReferralRequest;
import com.school.erp.dto.student.StudentReferralResponse;
import com.school.erp.service.StudentReferralService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/student-referrals")
public class StudentReferralController {

    private final StudentReferralService referralService;

    public StudentReferralController(StudentReferralService referralService) {
        this.referralService = referralService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<StudentReferralResponse>>> getAllReferrals(
            @RequestParam(required = false) Long schoolId
    ) {
        return ResponseEntity.ok(ApiResponse.success(referralService.getAllReferrals(schoolId), "Student referrals retrieved successfully"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<StudentReferralResponse>> createReferral(
            @Valid @RequestBody StudentReferralRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(referralService.createReferral(request), "Student referral created successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<StudentReferralResponse>> updateReferral(
            @PathVariable Long id,
            @Valid @RequestBody StudentReferralRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(referralService.updateReferral(id, request), "Student referral updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteReferral(
            @PathVariable Long id,
            @RequestParam(required = false) Long schoolId
    ) {
        referralService.deleteReferral(id, schoolId);
        return ResponseEntity.ok(ApiResponse.success(null, "Student referral deleted successfully"));
    }
}
