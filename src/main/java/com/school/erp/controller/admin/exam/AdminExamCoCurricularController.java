package com.school.erp.controller.admin.exam;

import com.school.erp.api.ApiResponse;
import com.school.erp.api.PaginationMeta;
import com.school.erp.dto.exam.CoCurricularGradeRequest;
import com.school.erp.dto.exam.CoCurricularItemResponse;
import com.school.erp.dto.exam.CoCurricularStatsResponse;
import com.school.erp.security.PermissionRequired;
import com.school.erp.service.exam.ExamCoCurricularService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/v1/admin/exams/co-curricular", "/api/v1/exams/co-curricular"})
@RequiredArgsConstructor
public class AdminExamCoCurricularController {

    private final ExamCoCurricularService coCurricularService;

    @GetMapping
    @PermissionRequired("exams_results.co_curricular_grades.view")
    public ResponseEntity<ApiResponse<List<CoCurricularItemResponse>>> getCoCurricular(
            @RequestParam(required = false) Long schoolId,
            @RequestParam(required = false) Long termId,
            @RequestParam(required = false) Long classId,
            @RequestParam(required = false) Long sectionId,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<CoCurricularItemResponse> result = coCurricularService.filterCoCurricular(
                schoolId, termId, classId, sectionId, search, PageRequest.of(page, size));

        return ResponseEntity.ok(ApiResponse.success(
                result.getContent(),
                "Co-curricular assessments retrieved successfully",
                new PaginationMeta(result.getNumber(), result.getSize(), result.getTotalElements())));
    }

    @PostMapping("/save")
    @PermissionRequired("exams_results.co_curricular_grades.edit")
    public ResponseEntity<ApiResponse<CoCurricularItemResponse>> saveGrade(
            @RequestParam(required = false) Long schoolId,
            @Valid @RequestBody CoCurricularGradeRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                coCurricularService.saveGrade(schoolId, request),
                "Co-curricular grade saved successfully"));
    }

    @PostMapping("/submit")
    @PermissionRequired("exams_results.co_curricular_grades.edit")
    public ResponseEntity<ApiResponse<Integer>> submitSection(
            @RequestParam(required = false) Long schoolId,
            @RequestParam Long termId,
            @RequestParam Long classId,
            @RequestParam(required = false) Long sectionId) {
        int count = coCurricularService.submitSection(schoolId, termId, classId, sectionId);
        return ResponseEntity.ok(ApiResponse.success(
                count,
                count + " entries submitted to final board successfully"));
    }

    @GetMapping("/stats")
    @PermissionRequired("exams_results.co_curricular_grades.view")
    public ResponseEntity<ApiResponse<CoCurricularStatsResponse>> getStats(
            @RequestParam(required = false) Long schoolId,
            @RequestParam(required = false) Long termId,
            @RequestParam(required = false) Long classId) {
        return ResponseEntity.ok(ApiResponse.success(
                coCurricularService.getStats(schoolId, termId, classId),
                "Co-curricular assessment statistics retrieved successfully"));
    }
}
