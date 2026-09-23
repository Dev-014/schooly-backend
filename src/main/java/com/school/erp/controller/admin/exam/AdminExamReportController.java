package com.school.erp.controller.admin.exam;

import com.school.erp.api.ApiResponse;
import com.school.erp.api.PaginationMeta;
import com.school.erp.dto.exam.*;
import com.school.erp.security.PermissionRequired;
import com.school.erp.service.exam.ExamReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/v1/admin/exams/reports", "/api/v1/exams/reports"})
@RequiredArgsConstructor
public class AdminExamReportController {

    private final ExamReportService examReportService;

    @GetMapping("/analytics")
    @PermissionRequired("exams_results.examination_reports.view")
    public ResponseEntity<ApiResponse<ExamReportAnalyticsResponse>> getAnalytics(
            @RequestParam(required = false) Long schoolId,
            @RequestParam(required = false) String academicYear,
            @RequestParam(required = false) Long termId,
            @RequestParam(required = false) Long classId,
            @RequestParam(required = false) Long sectionId,
            @RequestParam(required = false) Long examSetupId) {

        return ResponseEntity.ok(ApiResponse.success(
                examReportService.getAnalytics(schoolId, academicYear, termId, classId, sectionId, examSetupId),
                "Examination analytics retrieved successfully"));
    }

    @GetMapping("/metrics")
    @PermissionRequired("exams_results.examination_reports.view")
    public ResponseEntity<ApiResponse<ExamReportMetricsResponse>> getMetrics(
            @RequestParam(required = false) Long schoolId,
            @RequestParam(required = false) Long termId,
            @RequestParam(required = false) Long classId,
            @RequestParam(required = false) Long sectionId,
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) Long examSetupId) {

        return ResponseEntity.ok(ApiResponse.success(
                examReportService.getMetrics(schoolId, termId, classId, sectionId, subjectId, examSetupId),
                "Examination metrics retrieved successfully"));
    }

    @GetMapping("/detailed-preview")
    @PermissionRequired("exams_results.examination_reports.view")
    public ResponseEntity<ApiResponse<List<ExamReportPreviewItemResponse>>> getDetailedPreview(
            @RequestParam(required = false) Long schoolId,
            @RequestParam(required = false) Long termId,
            @RequestParam(required = false) Long classId,
            @RequestParam(required = false) Long sectionId,
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) Long examSetupId,
            @RequestParam(defaultValue = "TERM_WISE") String reportType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<ExamReportPreviewItemResponse> result = examReportService.getDetailedPreview(
                schoolId, termId, classId, sectionId, subjectId, examSetupId, reportType, PageRequest.of(page, size));

        return ResponseEntity.ok(ApiResponse.success(
                result.getContent(),
                "Detailed result preview retrieved successfully",
                new PaginationMeta(result.getNumber(), result.getSize(), result.getTotalElements())));
    }

    @PostMapping("/generate-pdf")
    @PermissionRequired("exams_results.examination_reports.edit")
    public ResponseEntity<ApiResponse<PdfReportResponse>> generatePdf(
            @RequestParam(required = false) Long schoolId,
            @Valid @RequestBody GeneratePdfReportRequest request) {

        return ResponseEntity.ok(ApiResponse.success(
                examReportService.generatePdfReport(schoolId, request),
                "Report PDF generated successfully"));
    }
}
