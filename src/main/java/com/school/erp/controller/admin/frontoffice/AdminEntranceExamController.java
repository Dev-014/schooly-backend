package com.school.erp.controller.admin.frontoffice;

import com.school.erp.api.ApiResponse;
import com.school.erp.api.PaginationMeta;
import com.school.erp.dto.frontoffice.EntranceExamRequest;
import com.school.erp.dto.frontoffice.EntranceExamResponse;
import com.school.erp.dto.frontoffice.EntranceExamStatsResponse;
import com.school.erp.security.PermissionRequired;
import com.school.erp.service.frontoffice.SchoolEntranceExamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/v1/admin/front-office/entrance-exams", "/api/v1/front-office/entrance-exams"})
@RequiredArgsConstructor
public class AdminEntranceExamController {

    private final SchoolEntranceExamService entranceExamService;

    @GetMapping
    @PermissionRequired("front_office.entrance_exam.view")
    public ResponseEntity<ApiResponse<List<EntranceExamResponse>>> getEntranceExams(
            @RequestParam(required = false) Long schoolId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String className,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<EntranceExamResponse> result = entranceExamService.filterEntranceExams(
                schoolId, search, className, status, PageRequest.of(page, size));

        return ResponseEntity.ok(ApiResponse.success(
                result.getContent(),
                "Entrance exam candidates retrieved successfully",
                new PaginationMeta(result.getNumber(), result.getSize(), result.getTotalElements())));
    }

    @GetMapping("/{id}")
    @PermissionRequired("front_office.entrance_exam.view")
    public ResponseEntity<ApiResponse<EntranceExamResponse>> getEntranceExamById(
            @RequestParam(required = false) Long schoolId,
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                entranceExamService.getEntranceExamById(schoolId, id),
                "Entrance exam candidate details retrieved successfully"));
    }

    @PostMapping
    @PermissionRequired("front_office.entrance_exam.view")
    public ResponseEntity<ApiResponse<EntranceExamResponse>> createEntranceExam(
            @RequestParam(required = false) Long schoolId,
            @Valid @RequestBody EntranceExamRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                entranceExamService.createEntranceExam(schoolId, request),
                "Entrance exam candidate registered successfully"));
    }

    @PutMapping("/{id}")
    @PermissionRequired("front_office.entrance_exam.view")
    public ResponseEntity<ApiResponse<EntranceExamResponse>> updateEntranceExam(
            @RequestParam(required = false) Long schoolId,
            @PathVariable Long id,
            @Valid @RequestBody EntranceExamRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                entranceExamService.updateEntranceExam(schoolId, id, request),
                "Entrance exam candidate updated successfully"));
    }

    @PutMapping("/{id}/status")
    @PermissionRequired("front_office.entrance_exam.view")
    public ResponseEntity<ApiResponse<EntranceExamResponse>> updateStatus(
            @RequestParam(required = false) Long schoolId,
            @PathVariable Long id,
            @RequestParam String status) {
        return ResponseEntity.ok(ApiResponse.success(
                entranceExamService.updateStatus(schoolId, id, status),
                "Entrance exam status updated successfully"));
    }

    @GetMapping("/stats")
    @PermissionRequired("front_office.entrance_exam.view")
    public ResponseEntity<ApiResponse<EntranceExamStatsResponse>> getStats(
            @RequestParam(required = false) Long schoolId) {
        return ResponseEntity.ok(ApiResponse.success(
                entranceExamService.getStats(schoolId),
                "Entrance exam statistics retrieved successfully"));
    }

    @DeleteMapping("/{id}")
    @PermissionRequired("front_office.entrance_exam.view")
    public ResponseEntity<ApiResponse<Void>> deleteEntranceExam(
            @RequestParam(required = false) Long schoolId,
            @PathVariable Long id) {
        entranceExamService.deleteEntranceExam(schoolId, id);
        return ResponseEntity.ok(ApiResponse.success(null, "Entrance exam candidate deleted successfully"));
    }
}
