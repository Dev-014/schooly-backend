package com.school.erp.controller.admin.exam;

import com.school.erp.api.ApiResponse;
import com.school.erp.dto.exam.ExamSetupRequest;
import com.school.erp.dto.exam.ExamSetupResponse;
import com.school.erp.dto.exam.ExamSetupStatsResponse;
import com.school.erp.security.PermissionRequired;
import com.school.erp.service.exam.ExamSetupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/v1/admin/exams/setups", "/api/v1/exams/setups"})
@RequiredArgsConstructor
public class AdminExamSetupController {

    private final ExamSetupService examSetupService;

    @GetMapping
    @PermissionRequired("exams_results.exam_setup_student.view")
    public ResponseEntity<ApiResponse<List<ExamSetupResponse>>> getSetups(
            @RequestParam(required = false) Long schoolId,
            @RequestParam(required = false) Long termId) {
        return ResponseEntity.ok(ApiResponse.success(
                examSetupService.getSetups(schoolId, termId),
                "Exam configurations retrieved successfully"));
    }

    @GetMapping("/{id}")
    @PermissionRequired("exams_results.exam_setup_student.view")
    public ResponseEntity<ApiResponse<ExamSetupResponse>> getSetupById(
            @RequestParam(required = false) Long schoolId,
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                examSetupService.getSetupById(schoolId, id),
                "Exam configuration details retrieved successfully"));
    }

    @PostMapping
    @PermissionRequired("exams_results.exam_setup_student.edit")
    public ResponseEntity<ApiResponse<ExamSetupResponse>> createSetup(
            @RequestParam(required = false) Long schoolId,
            @Valid @RequestBody ExamSetupRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                examSetupService.createSetup(schoolId, request),
                "Exam configuration created successfully"));
    }

    @PutMapping("/{id}")
    @PermissionRequired("exams_results.exam_setup_student.edit")
    public ResponseEntity<ApiResponse<ExamSetupResponse>> updateSetup(
            @RequestParam(required = false) Long schoolId,
            @PathVariable Long id,
            @Valid @RequestBody ExamSetupRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                examSetupService.updateSetup(schoolId, id, request),
                "Exam configuration updated successfully"));
    }

    @DeleteMapping("/{id}")
    @PermissionRequired("exams_results.exam_setup_student.edit")
    public ResponseEntity<ApiResponse<Void>> deleteSetup(
            @RequestParam(required = false) Long schoolId,
            @PathVariable Long id) {
        examSetupService.deleteSetup(schoolId, id);
        return ResponseEntity.ok(ApiResponse.success(null, "Exam configuration deleted successfully"));
    }

    @GetMapping("/stats")
    @PermissionRequired("exams_results.exam_setup_student.view")
    public ResponseEntity<ApiResponse<ExamSetupStatsResponse>> getStats(
            @RequestParam(required = false) Long schoolId,
            @RequestParam(required = false) Long termId) {
        return ResponseEntity.ok(ApiResponse.success(
                examSetupService.getStats(schoolId, termId),
                "Exam configuration statistics retrieved successfully"));
    }
}
