package com.school.erp.controller.admin.exam;

import com.school.erp.api.ApiResponse;
import com.school.erp.api.PaginationMeta;
import com.school.erp.dto.exam.TeacherRemarkItemResponse;
import com.school.erp.dto.exam.TeacherRemarkRequest;
import com.school.erp.dto.exam.TeacherRemarkStatsResponse;
import com.school.erp.security.PermissionRequired;
import com.school.erp.service.exam.ExamTeacherRemarkService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/v1/admin/exams/teacher-remarks", "/api/v1/admin/exams/remarks", "/api/v1/exams/teacher-remarks", "/api/v1/exams/remarks"})
@RequiredArgsConstructor
public class AdminExamTeacherRemarkController {

    private final ExamTeacherRemarkService teacherRemarkService;

    @GetMapping
    @PermissionRequired("exams_results.teacher_remark.view")
    public ResponseEntity<ApiResponse<List<TeacherRemarkItemResponse>>> getRemarks(
            @RequestParam(required = false) Long schoolId,
            @RequestParam(required = false) Long termId,
            @RequestParam(required = false) Long classId,
            @RequestParam(required = false) Long sectionId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<TeacherRemarkItemResponse> result = teacherRemarkService.filterRemarks(
                schoolId, termId, classId, sectionId, PageRequest.of(page, size));

        return ResponseEntity.ok(ApiResponse.success(
                result.getContent(),
                "Student qualitative assessment remarks retrieved successfully",
                new PaginationMeta(result.getNumber(), result.getSize(), result.getTotalElements())));
    }

    @PostMapping("/save")
    @PermissionRequired("exams_results.teacher_remark.edit")
    public ResponseEntity<ApiResponse<TeacherRemarkItemResponse>> saveRemark(
            @RequestParam(required = false) Long schoolId,
            @Valid @RequestBody TeacherRemarkRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                teacherRemarkService.saveRemark(schoolId, request),
                "Teacher remark saved successfully"));
    }

    @GetMapping("/stats")
    @PermissionRequired("exams_results.teacher_remark.view")
    public ResponseEntity<ApiResponse<TeacherRemarkStatsResponse>> getStats(
            @RequestParam(required = false) Long schoolId,
            @RequestParam(required = false) Long termId,
            @RequestParam(required = false) Long classId) {
        return ResponseEntity.ok(ApiResponse.success(
                teacherRemarkService.getStats(schoolId, termId, classId),
                "Teacher remarks progress statistics retrieved successfully"));
    }
}
