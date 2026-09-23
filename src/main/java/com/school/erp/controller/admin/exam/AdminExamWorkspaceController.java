package com.school.erp.controller.admin.exam;

import com.school.erp.api.ApiResponse;
import com.school.erp.dto.exam.workspace.ExamWorkspaceOverviewResponse;
import com.school.erp.dto.exam.workspace.ExamSubjectConfigDto;
import com.school.erp.dto.exam.workspace.ExamApplicabilityDto;
import com.school.erp.security.PermissionRequired;
import com.school.erp.service.exam.ExamWorkspaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import com.school.erp.dto.exam.workspace.ExamSubjectBulkAddRequest;
import com.school.erp.dto.exam.workspace.ExamSubjectConfigUpdateRequest;
import com.school.erp.dto.exam.workspace.ExamApplicabilitySyncRequest;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/exams/setups/{examId}/workspace")
@RequiredArgsConstructor
public class AdminExamWorkspaceController {

    private final ExamWorkspaceService examWorkspaceService;

    @GetMapping("/overview")
    @PermissionRequired("exams_results.exam_setup_student.view")
    public ResponseEntity<ApiResponse<ExamWorkspaceOverviewResponse>> getOverview(
            @RequestParam(required = false) Long schoolId,
            @PathVariable Long examId) {
        return ResponseEntity.ok(ApiResponse.success(
                examWorkspaceService.getOverview(schoolId, examId),
                "Exam workspace overview retrieved successfully"));
    }

        @GetMapping("/schedules")
    @PermissionRequired("exams_results.exam_setup_student.edit")
    public ResponseEntity<ApiResponse<List<com.school.erp.dto.exam.ExamScheduleResponse>>> getSchedules(
            @RequestParam(required = false) Long schoolId,
            @PathVariable Long examId) {
        List<com.school.erp.dto.exam.ExamScheduleResponse> schedules = examWorkspaceService.getExamSchedules(schoolId, examId);
        return ResponseEntity.ok(ApiResponse.success(schedules, "Exam schedules retrieved successfully"));
    }

    @PostMapping("/schedules")
    @PermissionRequired("exams_results.exam_setup_student.edit")
    public ResponseEntity<ApiResponse<Void>> saveSchedules(
            @RequestParam(required = false) Long schoolId,
            @PathVariable Long examId,
            @Valid @RequestBody com.school.erp.dto.exam.BulkExamScheduleRequest request) {
        examWorkspaceService.saveExamSchedules(schoolId, examId, request);
        return ResponseEntity.ok(ApiResponse.success(null, "Exam schedules saved successfully"));
    }
    @GetMapping("/subjects")
    @PermissionRequired("exams_results.exam_setup_student.view")
    public ResponseEntity<ApiResponse<List<ExamSubjectConfigDto>>> getSubjects(
            @RequestParam(required = false) Long schoolId,
            @PathVariable Long examId) {
        return ResponseEntity.ok(ApiResponse.success(
                examWorkspaceService.getSubjects(schoolId, examId),
                "Exam subjects retrieved successfully"));
    }

    @GetMapping("/applicability")
    @PermissionRequired("exams_results.exam_setup_student.view")
    public ResponseEntity<ApiResponse<List<ExamApplicabilityDto>>> getApplicabilities(
            @RequestParam(required = false) Long schoolId,
            @PathVariable Long examId) {
        return ResponseEntity.ok(ApiResponse.success(
                examWorkspaceService.getApplicabilities(schoolId, examId),
                "Exam applicabilities retrieved successfully"));
    }

    @GetMapping("/students")
    @PermissionRequired("exams_results.exam_setup_student.view")
    public ResponseEntity<ApiResponse<List<com.school.erp.dto.exam.workspace.ExamStudentEligibilityDto>>> getStudents(
            @RequestParam(required = false) Long schoolId,
            @PathVariable Long examId) {
        return ResponseEntity.ok(ApiResponse.success(
                examWorkspaceService.getEligibleStudents(schoolId, examId),
                "Exam eligible students retrieved successfully"));
    }

    @PutMapping("/students/{studentId}/status")
    @PermissionRequired("exams_results.exam_setup_student.edit")
    public ResponseEntity<ApiResponse<Void>> updateStudentStatus(
            @RequestParam(required = false) Long schoolId,
            @PathVariable Long examId,
            @PathVariable Long studentId,
            @org.springframework.web.bind.annotation.RequestBody com.school.erp.dto.exam.workspace.ExamStudentEligibilityStatusUpdateRequest request) {
        examWorkspaceService.updateStudentEligibility(schoolId, examId, studentId, request);
        return ResponseEntity.ok(ApiResponse.success(null, "Exam student eligibility status updated successfully"));
    }
    
    @PutMapping("/students/subjects/{eligibilityId}/status")
    @PermissionRequired("exams_results.exam_setup_student.edit")
    public ResponseEntity<ApiResponse<Void>> updateStudentSubjectStatus(
            @RequestParam(required = false) Long schoolId,
            @PathVariable Long examId,
            @PathVariable Long eligibilityId,
            @org.springframework.web.bind.annotation.RequestBody com.school.erp.dto.exam.workspace.ExamStudentEligibilityStatusUpdateRequest request) {
        examWorkspaceService.updateStudentSubjectEligibility(schoolId, examId, eligibilityId, request);
        return ResponseEntity.ok(ApiResponse.success(null, "Exam student subject eligibility status updated successfully"));
    }

    @PostMapping("/subjects")
    @PermissionRequired("exams_results.exam_setup_student.edit")
    public ResponseEntity<ApiResponse<Void>> addSubjects(
            @RequestParam(required = false) Long schoolId,
            @PathVariable Long examId,
            @Valid @RequestBody ExamSubjectBulkAddRequest request) {
        examWorkspaceService.addSubjects(schoolId, examId, request);
        return ResponseEntity.ok(ApiResponse.success(null, "Subjects added successfully"));
    }

    @DeleteMapping("/subjects/{configId}")
    @PermissionRequired("exams_results.exam_setup_student.edit")
    public ResponseEntity<ApiResponse<Void>> removeSubject(
            @RequestParam(required = false) Long schoolId,
            @PathVariable Long examId,
            @PathVariable Long configId) {
        examWorkspaceService.removeSubject(schoolId, examId, configId);
        return ResponseEntity.ok(ApiResponse.success(null, "Subject removed successfully"));
    }

    @PutMapping("/subjects/{configId}")
    @PermissionRequired("exams_results.exam_setup_student.edit")
    public ResponseEntity<ApiResponse<ExamSubjectConfigDto>> updateSubjectConfig(
            @RequestParam(required = false) Long schoolId,
            @PathVariable Long examId,
            @PathVariable Long configId,
            @Valid @RequestBody ExamSubjectConfigUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                examWorkspaceService.updateSubjectConfig(schoolId, examId, configId, request),
                "Subject config updated successfully"));
    }

    @PostMapping("/applicability")
    @PermissionRequired("exams_results.exam_setup_student.edit")
    public ResponseEntity<ApiResponse<Void>> syncApplicabilities(
            @RequestParam(required = false) Long schoolId,
            @PathVariable Long examId,
            @Valid @RequestBody ExamApplicabilitySyncRequest request) {
        examWorkspaceService.syncApplicabilities(schoolId, examId, request);
        return ResponseEntity.ok(ApiResponse.success(null, "Classes synced successfully"));
    }
}
