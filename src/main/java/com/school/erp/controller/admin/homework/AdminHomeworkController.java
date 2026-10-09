package com.school.erp.controller.admin.homework;

import com.school.erp.api.ApiResponse;
import com.school.erp.api.PaginationMeta;
import com.school.erp.dto.homework.*;
import com.school.erp.entity.homework.AssignmentStatus;
import com.school.erp.entity.homework.AssignmentType;
import com.school.erp.security.PermissionRequired;
import com.school.erp.service.homework.HomeworkService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping({"/api/v1/admin/homework", "/api/v1/homework"})
@RequiredArgsConstructor
public class AdminHomeworkController {

    private final HomeworkService homeworkService;

    // ─── Filter Assignments (Homework or Classwork) ──────────────────────────────

    @GetMapping
    @PermissionRequired("homework.assignment.view")
    public ResponseEntity<ApiResponse<List<HomeworkAssignmentResponse>>> getAssignments(
            @RequestParam(required = false) Long schoolId,
            @RequestParam(required = false) AssignmentType type,
            @RequestParam(required = false) Long classId,
            @RequestParam(required = false) Long sectionId,
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) AssignmentStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<HomeworkAssignmentResponse> result = homeworkService.filterAssignments(
                schoolId, type, classId, sectionId, subjectId, status, startDate, endDate, search, PageRequest.of(page, size));

        return ResponseEntity.ok(ApiResponse.success(
                result.getContent(),
                "Assignments retrieved successfully",
                new PaginationMeta(result.getNumber(), result.getSize(), result.getTotalElements())));
    }

    // ─── Get Assignment By ID ────────────────────────────────────────────────────

    @GetMapping("/{id}")
    @PermissionRequired("homework.assignment.view")
    public ResponseEntity<ApiResponse<HomeworkAssignmentResponse>> getAssignmentById(
            @RequestParam(required = false) Long schoolId,
            @PathVariable Long id) {

        return ResponseEntity.ok(ApiResponse.success(
                homeworkService.getAssignmentById(schoolId, id),
                "Assignment retrieved successfully"));
    }

    // ─── Create Assignment ───────────────────────────────────────────────────────

    @PostMapping
    @PermissionRequired("homework.assignment.edit")
    public ResponseEntity<ApiResponse<HomeworkAssignmentResponse>> createAssignment(
            @RequestParam(required = false) Long schoolId,
            @Valid @RequestBody HomeworkAssignmentRequest request) {

        HomeworkAssignmentResponse created = homeworkService.createAssignment(schoolId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                created,
                "Assignment created successfully"));
    }

    // ─── Update Assignment ───────────────────────────────────────────────────────

    @PutMapping("/{id}")
    @PermissionRequired("homework.assignment.edit")
    public ResponseEntity<ApiResponse<HomeworkAssignmentResponse>> updateAssignment(
            @RequestParam(required = false) Long schoolId,
            @PathVariable Long id,
            @Valid @RequestBody HomeworkAssignmentRequest request) {

        return ResponseEntity.ok(ApiResponse.success(
                homeworkService.updateAssignment(schoolId, id, request),
                "Assignment updated successfully"));
    }

    // ─── Delete Assignment ───────────────────────────────────────────────────────

    @DeleteMapping("/{id}")
    @PermissionRequired("homework.assignment.edit")
    public ResponseEntity<ApiResponse<Void>> deleteAssignment(
            @RequestParam(required = false) Long schoolId,
            @PathVariable Long id) {

        homeworkService.deleteAssignment(schoolId, id);
        return ResponseEntity.ok(ApiResponse.success(null, "Assignment deleted successfully"));
    }

    // ─── View Submissions for an Assignment ───────────────────────────────────────

    @GetMapping("/{id}/submissions")
    @PermissionRequired("homework.submission.view")
    public ResponseEntity<ApiResponse<List<HomeworkSubmissionResponse>>> getSubmissions(
            @RequestParam(required = false) Long schoolId,
            @PathVariable Long id) {

        return ResponseEntity.ok(ApiResponse.success(
                homeworkService.getSubmissionsForAssignment(schoolId, id),
                "Submissions retrieved successfully"));
    }

    // ─── Grade / Evaluate Submission ─────────────────────────────────────────────

    @PutMapping("/submissions/{submissionId}/evaluate")
    @PermissionRequired("homework.submission.grade")
    public ResponseEntity<ApiResponse<HomeworkSubmissionResponse>> evaluateSubmission(
            @RequestParam(required = false) Long schoolId,
            @PathVariable Long submissionId,
            @RequestBody HomeworkEvaluationRequest request) {

        return ResponseEntity.ok(ApiResponse.success(
                homeworkService.evaluateSubmission(schoolId, submissionId, request),
                "Submission evaluated successfully"));
    }

    // ─── Unassigned Subjects/Classes Report ──────────────────────────────────────

    @GetMapping("/reports/unassigned")
    @PermissionRequired("homework.assignment.view")
    public ResponseEntity<ApiResponse<List<UnassignedReportItemResponse>>> getUnassignedReport(
            @RequestParam(required = false) Long schoolId,
            @RequestParam(required = false) Long classId,
            @RequestParam(required = false) Long sectionId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        return ResponseEntity.ok(ApiResponse.success(
                homeworkService.getUnassignedReport(schoolId, classId, sectionId, date),
                "Unassigned report retrieved successfully"));
    }

    // ─── Summary Stats ───────────────────────────────────────────────────────────

    @GetMapping("/stats")
    @PermissionRequired("homework.assignment.view")
    public ResponseEntity<ApiResponse<HomeworkStatsResponse>> getStats(
            @RequestParam(required = false) Long schoolId,
            @RequestParam(required = false) AssignmentType type) {

        return ResponseEntity.ok(ApiResponse.success(
                homeworkService.getHomeworkStats(schoolId, type),
                "Homework stats retrieved successfully"));
    }
}
