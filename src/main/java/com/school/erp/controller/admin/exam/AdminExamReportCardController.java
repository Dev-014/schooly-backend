package com.school.erp.controller.admin.exam;

import com.school.erp.api.ApiResponse;
import com.school.erp.dto.exam.*;
import com.school.erp.security.PermissionRequired;
import com.school.erp.service.exam.ExamReportCardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/v1/admin/exams/report-cards", "/api/v1/exams/report-cards"})
@RequiredArgsConstructor
public class AdminExamReportCardController {

    private final ExamReportCardService examReportCardService;

    @GetMapping("/queue-status")
    @PermissionRequired("exams_results.report_card.view")
    public ResponseEntity<ApiResponse<ReportCardQueueStatusResponse>> getQueueStatus(
            @RequestParam(required = false) Long schoolId) {

        return ResponseEntity.ok(ApiResponse.success(
                examReportCardService.getQueueStatus(schoolId),
                "Queue status retrieved successfully"));
    }

    @GetMapping("/recently-generated")
    @PermissionRequired("exams_results.report_card.view")
    public ResponseEntity<ApiResponse<List<RecentlyGeneratedBatchResponse>>> getRecentlyGenerated(
            @RequestParam(required = false) Long schoolId) {

        return ResponseEntity.ok(ApiResponse.success(
                examReportCardService.getRecentlyGenerated(schoolId),
                "Recently generated reports retrieved successfully"));
    }

    @GetMapping("/criteria-count")
    @PermissionRequired("exams_results.report_card.view")
    public ResponseEntity<ApiResponse<ReportCardCriteriaCountResponse>> getCriteriaCount(
            @RequestParam(required = false) Long schoolId,
            @RequestParam(required = false) Long termId,
            @RequestParam(required = false) Long examSetupId,
            @RequestParam(required = false) Long classId,
            @RequestParam(required = false) Long sectionId) {

        return ResponseEntity.ok(ApiResponse.success(
                examReportCardService.getCriteriaStudentCount(schoolId, termId, examSetupId, classId, sectionId),
                "Criteria student count retrieved successfully"));
    }

    @PostMapping("/generate")
    @PermissionRequired("exams_results.report_card.edit")
    public ResponseEntity<ApiResponse<ReportCardQueueStatusResponse>> generateReportCards(
            @RequestParam(required = false) Long schoolId,
            @Valid @RequestBody GenerateReportCardRequest request) {

        return ResponseEntity.ok(ApiResponse.success(
                examReportCardService.generateReportCards(schoolId, request),
                "Report cards generation initiated successfully"));
    }

    @GetMapping("/templates")
    @PermissionRequired("exams_results.report_card.view")
    public ResponseEntity<ApiResponse<List<ReportCardTemplateResponse>>> getTemplates() {

        return ResponseEntity.ok(ApiResponse.success(
                examReportCardService.getTemplates(),
                "Report card templates retrieved successfully"));
    }

    @GetMapping("/student/{studentId}")
    @PermissionRequired("exams_results.report_card.view")
    public ResponseEntity<ApiResponse<List<StudentReportCardResponse>>> getStudentReportCards(
            @RequestParam(required = false) Long schoolId,
            @PathVariable Long studentId) {

        return ResponseEntity.ok(ApiResponse.success(
                examReportCardService.getStudentReportCards(schoolId, studentId),
                "Student report cards retrieved successfully"));
    }
}
