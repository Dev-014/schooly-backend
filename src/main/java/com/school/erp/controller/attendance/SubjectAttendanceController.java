package com.school.erp.controller;

import com.school.erp.api.ApiResponse;
import com.school.erp.dto.attendance.BulkSubjectAttendanceRequest;
import com.school.erp.dto.attendance.StudentSubjectAttendanceSummaryDTO;
import com.school.erp.dto.attendance.SubjectAttendanceRequest;
import com.school.erp.dto.attendance.SubjectAttendanceResponse;
import com.school.erp.service.SubjectAttendanceService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping({"/api/attendance/subjects", "/api/v1/attendance/subjects"})
public class SubjectAttendanceController {

    private final SubjectAttendanceService subjectAttendanceService;

    public SubjectAttendanceController(SubjectAttendanceService subjectAttendanceService) {
        this.subjectAttendanceService = subjectAttendanceService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<SubjectAttendanceResponse>>> getSubjectAttendance(
            @RequestParam(required = false) Long schoolId,
            @RequestParam(required = false) Long classId,
            @RequestParam(required = false) Long sectionId,
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate attendanceDate,
            @RequestParam(required = false) Long studentId
    ) {
        if (classId != null && subjectId != null && attendanceDate != null) {
            return ResponseEntity.ok(ApiResponse.success(
                    subjectAttendanceService.getSubjectAttendanceByPeriod(schoolId, classId, sectionId, subjectId, attendanceDate),
                    "Subject attendance fetched successfully"
            ));
        }

        return ResponseEntity.ok(ApiResponse.success(
                subjectAttendanceService.getStudentSubjectAttendance(schoolId, studentId),
                "Student subject attendance fetched successfully"
        ));
    }

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<List<StudentSubjectAttendanceSummaryDTO>>> getStudentSubjectSummary(
            @RequestParam(required = false) Long schoolId,
            @RequestParam(required = false) Long studentId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                subjectAttendanceService.getStudentSubjectSummary(schoolId, studentId),
                "Student subject-wise attendance summary fetched successfully"
        ));
    }

    @PostMapping("/bulk")
    public ResponseEntity<ApiResponse<List<SubjectAttendanceResponse>>> createBulkSubjectAttendance(
            @Valid @RequestBody BulkSubjectAttendanceRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                subjectAttendanceService.saveBulkSubjectAttendance(request),
                "Bulk subject attendance recorded successfully"
        ));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SubjectAttendanceResponse>> createSubjectAttendance(
            @Valid @RequestBody SubjectAttendanceRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                subjectAttendanceService.createSubjectAttendance(request),
                "Subject attendance recorded successfully"
        ));
    }
}
