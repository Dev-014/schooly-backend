package com.school.erp.controller.admin.exam;

import com.school.erp.api.ApiResponse;
import com.school.erp.dto.exam.ExamTermRequest;
import com.school.erp.dto.exam.ExamTermResponse;
import com.school.erp.dto.exam.ExamTermStatsResponse;
import com.school.erp.security.PermissionRequired;
import com.school.erp.service.exam.ExamTermService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/v1/admin/exams/terms", "/api/v1/exams/terms"})
@RequiredArgsConstructor
public class AdminExamTermController {

    private final ExamTermService examTermService;

    @GetMapping
    @PermissionRequired("exams_results.term_setup.view")
    public ResponseEntity<ApiResponse<List<ExamTermResponse>>> getTerms(
            @RequestParam(required = false) Long schoolId,
            @RequestParam(required = false) Long academicYearId) {
        return ResponseEntity.ok(ApiResponse.success(
                examTermService.getTerms(schoolId, academicYearId),
                "Exam terms retrieved successfully"));
    }

    @GetMapping("/{id}")
    @PermissionRequired("exams_results.term_setup.view")
    public ResponseEntity<ApiResponse<ExamTermResponse>> getTermById(
            @RequestParam(required = false) Long schoolId,
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                examTermService.getTermById(schoolId, id),
                "Exam term details retrieved successfully"));
    }

    @PostMapping
    @PermissionRequired("exams_results.term_setup.edit")
    public ResponseEntity<ApiResponse<ExamTermResponse>> createTerm(
            @RequestParam(required = false) Long schoolId,
            @Valid @RequestBody ExamTermRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                examTermService.createTerm(schoolId, request),
                "Exam term created successfully"));
    }

    @PutMapping("/{id}")
    @PermissionRequired("exams_results.term_setup.edit")
    public ResponseEntity<ApiResponse<ExamTermResponse>> updateTerm(
            @RequestParam(required = false) Long schoolId,
            @PathVariable Long id,
            @Valid @RequestBody ExamTermRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                examTermService.updateTerm(schoolId, id, request),
                "Exam term updated successfully"));
    }

    @DeleteMapping("/{id}")
    @PermissionRequired("exams_results.term_setup.edit")
    public ResponseEntity<ApiResponse<Void>> deleteTerm(
            @RequestParam(required = false) Long schoolId,
            @PathVariable Long id) {
        examTermService.deleteTerm(schoolId, id);
        return ResponseEntity.ok(ApiResponse.success(null, "Exam term deleted successfully"));
    }

    @GetMapping("/stats")
    @PermissionRequired("exams_results.term_setup.view")
    public ResponseEntity<ApiResponse<ExamTermStatsResponse>> getStats(
            @RequestParam(required = false) Long schoolId,
            @RequestParam(required = false) Long academicYearId) {
        return ResponseEntity.ok(ApiResponse.success(
                examTermService.getStats(schoolId, academicYearId),
                "Term statistics retrieved successfully"));
    }
}
