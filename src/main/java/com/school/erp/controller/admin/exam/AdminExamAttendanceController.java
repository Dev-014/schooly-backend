package com.school.erp.controller.admin.exam;

import com.school.erp.api.ApiResponse;
import com.school.erp.api.PaginationMeta;
import com.school.erp.dto.exam.BulkExamAttendanceRequest;
import com.school.erp.dto.exam.ExamAttendanceItemResponse;
import com.school.erp.dto.exam.ExamAttendanceRecordRequest;
import com.school.erp.dto.exam.ExamSessionSummaryResponse;
import com.school.erp.security.PermissionRequired;
import com.school.erp.service.exam.ExamAttendanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/v1/admin/exams/attendance", "/api/v1/exams/attendance"})
@RequiredArgsConstructor
public class AdminExamAttendanceController {

    private final ExamAttendanceService examAttendanceService;

    @GetMapping
    @PermissionRequired("exams_results.exam_attendance.view")
    public ResponseEntity<ApiResponse<List<ExamAttendanceItemResponse>>> getAttendanceRoster(
            @RequestParam(required = false) Long schoolId,
            @RequestParam(required = false) Long termId,
            @RequestParam(required = false) Long classId,
            @RequestParam(required = false) Long sectionId,
            @RequestParam(required = false) Long examSetupId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<ExamAttendanceItemResponse> result = examAttendanceService.filterAttendance(
                schoolId, termId, classId, sectionId, examSetupId, status, search, PageRequest.of(page, size));

        return ResponseEntity.ok(ApiResponse.success(
                result.getContent(),
                "Examinee roster retrieved successfully",
                new PaginationMeta(result.getNumber(), result.getSize(), result.getTotalElements())));
    }

    @GetMapping("/session-summary")
    @PermissionRequired("exams_results.exam_attendance.view")
    public ResponseEntity<ApiResponse<ExamSessionSummaryResponse>> getSessionSummary(
            @RequestParam(required = false) Long schoolId,
            @RequestParam(required = false) Long termId,
            @RequestParam(required = false) Long classId,
            @RequestParam(required = false) Long sectionId) {

        return ResponseEntity.ok(ApiResponse.success(
                examAttendanceService.getSessionSummary(schoolId, termId, classId, sectionId),
                "Session summary retrieved successfully"));
    }

    @PostMapping("/record")
    @PermissionRequired("exams_results.exam_attendance.edit")
    public ResponseEntity<ApiResponse<ExamAttendanceItemResponse>> recordAttendance(
            @RequestParam(required = false) Long schoolId,
            @Valid @RequestBody ExamAttendanceRecordRequest request) {

        return ResponseEntity.ok(ApiResponse.success(
                examAttendanceService.recordAttendance(schoolId, request),
                "Attendance recorded successfully"));
    }

    @PostMapping("/bulk")
    @PermissionRequired("exams_results.exam_attendance.edit")
    public ResponseEntity<ApiResponse<List<ExamAttendanceItemResponse>>> recordBulkAttendance(
            @RequestParam(required = false) Long schoolId,
            @Valid @RequestBody BulkExamAttendanceRequest request) {

        return ResponseEntity.ok(ApiResponse.success(
                examAttendanceService.recordBulkAttendance(schoolId, request),
                "Bulk attendance recorded successfully"));
    }
}
