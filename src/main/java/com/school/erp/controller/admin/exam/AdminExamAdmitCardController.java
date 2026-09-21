package com.school.erp.controller.admin.exam;

import com.school.erp.api.ApiResponse;
import com.school.erp.api.PaginationMeta;
import com.school.erp.dto.exam.AdmitCardPreviewResponse;
import com.school.erp.dto.exam.AdmitCardRosterItemResponse;
import com.school.erp.dto.exam.AdmitCardStatsResponse;
import com.school.erp.dto.exam.GenerateAdmitCardRequest;
import com.school.erp.security.PermissionRequired;
import com.school.erp.service.exam.ExamAdmitCardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/v1/admin/exams/admit-cards", "/api/v1/exams/admit-cards"})
@RequiredArgsConstructor
public class AdminExamAdmitCardController {

    private final ExamAdmitCardService examAdmitCardService;

    @GetMapping
    @PermissionRequired("exams_results.admit_card_students.view")
    public ResponseEntity<ApiResponse<List<AdmitCardRosterItemResponse>>> getAdmitCards(
            @RequestParam(required = false) Long schoolId,
            @RequestParam(required = false) Long examSetupId,
            @RequestParam(required = false) Long classId,
            @RequestParam(required = false) Long sectionId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<AdmitCardRosterItemResponse> result = examAdmitCardService.filterAdmitCards(
                schoolId, examSetupId, classId, sectionId, status, PageRequest.of(page, size));

        return ResponseEntity.ok(ApiResponse.success(
                result.getContent(),
                "Admit card roster retrieved successfully",
                new PaginationMeta(result.getNumber(), result.getSize(), result.getTotalElements())));
    }

    @GetMapping("/{studentId}/preview")
    @PermissionRequired("exams_results.admit_card_students.view")
    public ResponseEntity<ApiResponse<AdmitCardPreviewResponse>> getPreview(
            @RequestParam(required = false) Long schoolId,
            @RequestParam Long examSetupId,
            @PathVariable Long studentId) {
        return ResponseEntity.ok(ApiResponse.success(
                examAdmitCardService.getPreview(schoolId, examSetupId, studentId),
                "Admit card live preview retrieved successfully"));
    }

    @PostMapping("/generate")
    @PermissionRequired("exams_results.admit_card_students.edit")
    public ResponseEntity<ApiResponse<List<AdmitCardRosterItemResponse>>> generateAdmitCards(
            @RequestParam(required = false) Long schoolId,
            @Valid @RequestBody GenerateAdmitCardRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                examAdmitCardService.generateAdmitCards(schoolId, request),
                "Admit cards generated successfully"));
    }

    @PostMapping("/release-all")
    @PermissionRequired("exams_results.admit_card_students.edit")
    public ResponseEntity<ApiResponse<Integer>> releaseAll(
            @RequestParam(required = false) Long schoolId,
            @RequestParam Long examSetupId) {
        int count = examAdmitCardService.releaseAllCards(schoolId, examSetupId);
        return ResponseEntity.ok(ApiResponse.success(
                count,
                count + " admit cards released successfully"));
    }

    @GetMapping("/stats")
    @PermissionRequired("exams_results.admit_card_students.view")
    public ResponseEntity<ApiResponse<AdmitCardStatsResponse>> getStats(
            @RequestParam(required = false) Long schoolId,
            @RequestParam(required = false) Long examSetupId,
            @RequestParam(required = false) Long classId) {
        return ResponseEntity.ok(ApiResponse.success(
                examAdmitCardService.getStats(schoolId, examSetupId, classId),
                "Admit card statistics retrieved successfully"));
    }
}
