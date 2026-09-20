package com.school.erp.controller.admin.exam;

import com.school.erp.api.ApiResponse;
import com.school.erp.api.PaginationMeta;
import com.school.erp.dto.exam.BulkExamScheduleRequest;
import com.school.erp.dto.exam.ExamScheduleRequest;
import com.school.erp.dto.exam.ExamScheduleResponse;
import com.school.erp.dto.exam.ExamScheduleStatsResponse;
import com.school.erp.security.PermissionRequired;
import com.school.erp.service.exam.ExamScheduleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/v1/admin/exams/schedules", "/api/v1/exams/schedules"})
@RequiredArgsConstructor
public class AdminExamScheduleController {

    private final ExamScheduleService examScheduleService;

    @GetMapping
    @PermissionRequired("exams_results.exam_schedule_student.view")
    public ResponseEntity<ApiResponse<List<ExamScheduleResponse>>> getSchedules(
            @RequestParam(required = false) Long schoolId,
            @RequestParam(required = false) Long examSetupId,
            @RequestParam(required = false) Long classId,
            @RequestParam(required = false) Long sectionId,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<ExamScheduleResponse> result = examScheduleService.filterSchedules(
                schoolId, examSetupId, classId, sectionId, search, PageRequest.of(page, size));

        return ResponseEntity.ok(ApiResponse.success(
                result.getContent(),
                "Exam schedules retrieved successfully",
                new PaginationMeta(result.getNumber(), result.getSize(), result.getTotalElements())));
    }

    @GetMapping("/{id}")
    @PermissionRequired("exams_results.exam_schedule_student.view")
    public ResponseEntity<ApiResponse<ExamScheduleResponse>> getScheduleById(
            @RequestParam(required = false) Long schoolId,
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                examScheduleService.getScheduleById(schoolId, id),
                "Exam schedule details retrieved successfully"));
    }

    @PostMapping
    @PermissionRequired("exams_results.exam_schedule_student.edit")
    public ResponseEntity<ApiResponse<ExamScheduleResponse>> createSchedule(
            @RequestParam(required = false) Long schoolId,
            @Valid @RequestBody ExamScheduleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                examScheduleService.createSchedule(schoolId, request),
                "Exam schedule created successfully"));
    }

    @PostMapping("/bulk")
    @PermissionRequired("exams_results.exam_schedule_student.edit")
    public ResponseEntity<ApiResponse<List<ExamScheduleResponse>>> createBulkSchedules(
            @RequestParam(required = false) Long schoolId,
            @Valid @RequestBody BulkExamScheduleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                examScheduleService.createBulkSchedules(schoolId, request),
                "Bulk exam schedules created successfully"));
    }

    @PutMapping("/{id}")
    @PermissionRequired("exams_results.exam_schedule_student.edit")
    public ResponseEntity<ApiResponse<ExamScheduleResponse>> updateSchedule(
            @RequestParam(required = false) Long schoolId,
            @PathVariable Long id,
            @Valid @RequestBody ExamScheduleRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                examScheduleService.updateSchedule(schoolId, id, request),
                "Exam schedule updated successfully"));
    }

    @DeleteMapping("/{id}")
    @PermissionRequired("exams_results.exam_schedule_student.edit")
    public ResponseEntity<ApiResponse<Void>> deleteSchedule(
            @RequestParam(required = false) Long schoolId,
            @PathVariable Long id) {
        examScheduleService.deleteSchedule(schoolId, id);
        return ResponseEntity.ok(ApiResponse.success(null, "Exam schedule deleted successfully"));
    }

    @PostMapping("/{id}/publish")
    @PermissionRequired("exams_results.exam_schedule_student.edit")
    public ResponseEntity<ApiResponse<ExamScheduleResponse>> publishSchedule(
            @RequestParam(required = false) Long schoolId,
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                examScheduleService.updateScheduleStatus(schoolId, id, "PUBLISHED"),
                "Exam schedule published successfully"));
    }

    @PostMapping("/{id}/unpublish")
    @PermissionRequired("exams_results.exam_schedule_student.edit")
    public ResponseEntity<ApiResponse<ExamScheduleResponse>> unpublishSchedule(
            @RequestParam(required = false) Long schoolId,
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                examScheduleService.updateScheduleStatus(schoolId, id, "DRAFT"),
                "Exam schedule unpublished successfully"));
    }

    @GetMapping("/stats")
    @PermissionRequired("exams_results.exam_schedule_student.view")
    public ResponseEntity<ApiResponse<ExamScheduleStatsResponse>> getStats(
            @RequestParam(required = false) Long schoolId,
            @RequestParam(required = false) Long examSetupId) {
        return ResponseEntity.ok(ApiResponse.success(
                examScheduleService.getStats(schoolId, examSetupId),
                "Exam schedule statistics retrieved successfully"));
    }
}
