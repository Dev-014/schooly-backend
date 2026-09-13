package com.school.erp.controller.admin.exam;

import com.school.erp.api.ApiResponse;
import com.school.erp.api.PaginationMeta;
import com.school.erp.dto.exam.BulkExamMarksRequest;
import com.school.erp.dto.exam.ClassMarksStatsResponse;
import com.school.erp.dto.exam.ExamMarkEntryRequest;
import com.school.erp.dto.exam.ExamMarkItemResponse;
import com.school.erp.security.PermissionRequired;
import com.school.erp.service.exam.ExamMarkService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/v1/admin/exams/marks", "/api/v1/exams/marks"})
@RequiredArgsConstructor
public class AdminExamMarksController {

    private final ExamMarkService examMarkService;

    @GetMapping
    @PermissionRequired("exams_results.marks_entry.view")
    public ResponseEntity<ApiResponse<List<ExamMarkItemResponse>>> getMarks(
            @RequestParam(required = false) Long schoolId,
            @RequestParam(required = false) Long examSetupId,
            @RequestParam(required = false) Long classId,
            @RequestParam(required = false) Long sectionId,
            @RequestParam(required = false) Long subjectId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<ExamMarkItemResponse> result = examMarkService.filterMarks(
                schoolId, examSetupId, classId, sectionId, subjectId, PageRequest.of(page, size));

        return ResponseEntity.ok(ApiResponse.success(
                result.getContent(),
                "Marks roster retrieved successfully",
                new PaginationMeta(result.getNumber(), result.getSize(), result.getTotalElements())));
    }

    @PostMapping("/save")
    @PermissionRequired("exams_results.marks_entry.edit")
    public ResponseEntity<ApiResponse<ExamMarkItemResponse>> saveMark(
            @RequestParam(required = false) Long schoolId,
            @Valid @RequestBody ExamMarkEntryRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                examMarkService.saveMark(schoolId, request),
                "Marks entry saved successfully"));
    }

    @PostMapping("/bulk")
    @PermissionRequired("exams_results.marks_entry.edit")
    public ResponseEntity<ApiResponse<List<ExamMarkItemResponse>>> saveBulkMarks(
            @RequestParam(required = false) Long schoolId,
            @Valid @RequestBody BulkExamMarksRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                examMarkService.saveBulkMarks(schoolId, request),
                "Bulk marks saved successfully"));
    }

    @GetMapping("/stats")
    @PermissionRequired("exams_results.marks_entry.view")
    public ResponseEntity<ApiResponse<ClassMarksStatsResponse>> getClassStats(
            @RequestParam(required = false) Long schoolId,
            @RequestParam(required = false) Long examSetupId,
            @RequestParam(required = false) Long classId,
            @RequestParam(required = false) Long subjectId) {
        return ResponseEntity.ok(ApiResponse.success(
                examMarkService.getClassStats(schoolId, examSetupId, classId, subjectId),
                "Class marks statistics retrieved successfully"));
    }
}
