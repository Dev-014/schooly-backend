package com.school.erp.controller.admin.exam;

import com.school.erp.api.ApiResponse;
import com.school.erp.dto.exam.DivisionRequest;
import com.school.erp.dto.exam.DivisionResponse;
import com.school.erp.security.PermissionRequired;
import com.school.erp.service.exam.ExamDivisionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/v1/admin/exams/divisions", "/api/v1/exams/divisions"})
@RequiredArgsConstructor
public class AdminExamDivisionController {

    private final ExamDivisionService divisionService;

    @GetMapping
    @PermissionRequired("exams_results.division.view")
    public ResponseEntity<ApiResponse<List<DivisionResponse>>> getDivisions(
            @RequestParam(required = false) Long schoolId) {
        return ResponseEntity.ok(ApiResponse.success(
                divisionService.getDivisions(schoolId),
                "Academic divisions retrieved successfully"));
    }

    @GetMapping("/{id}")
    @PermissionRequired("exams_results.division.view")
    public ResponseEntity<ApiResponse<DivisionResponse>> getDivisionById(
            @RequestParam(required = false) Long schoolId,
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                divisionService.getDivisionById(schoolId, id),
                "Academic division details retrieved successfully"));
    }

    @PostMapping
    @PermissionRequired("exams_results.division.edit")
    public ResponseEntity<ApiResponse<DivisionResponse>> createDivision(
            @RequestParam(required = false) Long schoolId,
            @Valid @RequestBody DivisionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                divisionService.createDivision(schoolId, request),
                "Academic division created successfully"));
    }

    @PutMapping("/{id}")
    @PermissionRequired("exams_results.division.edit")
    public ResponseEntity<ApiResponse<DivisionResponse>> updateDivision(
            @RequestParam(required = false) Long schoolId,
            @PathVariable Long id,
            @Valid @RequestBody DivisionRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                divisionService.updateDivision(schoolId, id, request),
                "Academic division updated successfully"));
    }

    @DeleteMapping("/{id}")
    @PermissionRequired("exams_results.division.edit")
    public ResponseEntity<ApiResponse<Void>> deleteDivision(
            @RequestParam(required = false) Long schoolId,
            @PathVariable Long id) {
        divisionService.deleteDivision(schoolId, id);
        return ResponseEntity.ok(ApiResponse.success(null, "Academic division deleted successfully"));
    }
}
