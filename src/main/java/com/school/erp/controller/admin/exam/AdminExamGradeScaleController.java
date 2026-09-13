package com.school.erp.controller.admin.exam;

import com.school.erp.api.ApiResponse;
import com.school.erp.dto.exam.GradeScaleRequest;
import com.school.erp.dto.exam.GradeScaleResponse;
import com.school.erp.dto.exam.GradeScaleStatsResponse;
import com.school.erp.security.PermissionRequired;
import com.school.erp.service.exam.ExamGradeScaleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/v1/admin/exams/grades", "/api/v1/exams/grades"})
@RequiredArgsConstructor
public class AdminExamGradeScaleController {

    private final ExamGradeScaleService gradeScaleService;

    @GetMapping
    @PermissionRequired("exams_results.grade_list.view")
    public ResponseEntity<ApiResponse<List<GradeScaleResponse>>> getGradeScales(
            @RequestParam(required = false) Long schoolId,
            @RequestParam(required = false) String targetClass) {
        return ResponseEntity.ok(ApiResponse.success(
                gradeScaleService.getGradeScales(schoolId, targetClass),
                "Grade scales retrieved successfully"));
    }

    @GetMapping("/{id}")
    @PermissionRequired("exams_results.grade_list.view")
    public ResponseEntity<ApiResponse<GradeScaleResponse>> getGradeScaleById(
            @RequestParam(required = false) Long schoolId,
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                gradeScaleService.getGradeScaleById(schoolId, id),
                "Grade scale details retrieved successfully"));
    }

    @PostMapping
    @PermissionRequired("exams_results.grade_list.edit")
    public ResponseEntity<ApiResponse<GradeScaleResponse>> createGradeScale(
            @RequestParam(required = false) Long schoolId,
            @Valid @RequestBody GradeScaleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                gradeScaleService.createGradeScale(schoolId, request),
                "Grade scale created successfully"));
    }

    @PutMapping("/{id}")
    @PermissionRequired("exams_results.grade_list.edit")
    public ResponseEntity<ApiResponse<GradeScaleResponse>> updateGradeScale(
            @RequestParam(required = false) Long schoolId,
            @PathVariable Long id,
            @Valid @RequestBody GradeScaleRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                gradeScaleService.updateGradeScale(schoolId, id, request),
                "Grade scale updated successfully"));
    }

    @DeleteMapping("/{id}")
    @PermissionRequired("exams_results.grade_list.edit")
    public ResponseEntity<ApiResponse<Void>> deleteGradeScale(
            @RequestParam(required = false) Long schoolId,
            @PathVariable Long id) {
        gradeScaleService.deleteGradeScale(schoolId, id);
        return ResponseEntity.ok(ApiResponse.success(null, "Grade scale deleted successfully"));
    }

    @GetMapping("/stats")
    @PermissionRequired("exams_results.grade_list.view")
    public ResponseEntity<ApiResponse<GradeScaleStatsResponse>> getStats(
            @RequestParam(required = false) Long schoolId) {
        return ResponseEntity.ok(ApiResponse.success(
                gradeScaleService.getStats(schoolId),
                "Grade scale statistics retrieved successfully"));
    }
}
