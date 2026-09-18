package com.school.erp.controller;

import com.school.erp.api.ApiResponse;
import com.school.erp.dto.attendance.AttendanceRequest;
import com.school.erp.dto.attendance.AttendanceResponse;
import com.school.erp.dto.attendance.BulkAttendanceRequest;
import com.school.erp.service.AttendanceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<AttendanceResponse>>> getAttendance(
            @RequestParam(required = false) Long schoolId,
            @RequestParam(required = false) Long studentId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                attendanceService.getAttendance(schoolId, studentId),
                "Attendance fetched successfully"
        ));
    }

    @GetMapping("/summary/today")
    public ResponseEntity<ApiResponse<com.school.erp.dto.attendance.AttendanceSummaryDTO>> getSummaryToday(
            @RequestParam(required = false) Long schoolId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                attendanceService.getSummaryToday(schoolId),
                "Daily attendance summary fetched successfully"
        ));
    }

    @GetMapping("/analytics/trend")
    public ResponseEntity<ApiResponse<List<com.school.erp.dto.attendance.analytics.AttendanceTrendDTO>>> getAttendanceTrends(
            @RequestParam(required = false) Long schoolId,
            @RequestParam(defaultValue = "30") int days
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                attendanceService.getAttendanceTrends(schoolId, days),
                "Attendance trends fetched successfully"
        ));
    }

    @GetMapping("/analytics/grade-wise")
    public ResponseEntity<ApiResponse<List<com.school.erp.dto.attendance.analytics.GradeAttendanceDTO>>> getGradeWiseAttendance(
            @RequestParam(required = false) Long schoolId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                attendanceService.getGradeWiseAttendance(schoolId),
                "Grade-wise attendance fetched successfully"
        ));
    }

    @GetMapping("/daily")
    public ResponseEntity<ApiResponse<List<AttendanceResponse>>> getAttendanceByDate(
            @RequestParam(required = false) Long schoolId,
            @RequestParam(required = false) Long classId,
            @RequestParam(required = false) Long sectionId,
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) java.time.LocalDate attendanceDate
    ) {
        if (classId != null && attendanceDate != null) {
            return ResponseEntity.ok(ApiResponse.success(
                    attendanceService.getAttendanceByDate(schoolId, classId, sectionId, attendanceDate),
                    "Daily attendance fetched successfully"
            ));
        }
        return ResponseEntity.ok(ApiResponse.success(
                attendanceService.getAttendance(schoolId, studentId),
                "Attendance fetched successfully"
        ));
    }

    @PostMapping("/bulk")
    public ResponseEntity<ApiResponse<List<AttendanceResponse>>> createBulkAttendance(@Valid @RequestBody BulkAttendanceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                attendanceService.saveBulkAttendance(request),
                "Bulk attendance saved successfully"
        ));
    }
    @PostMapping
    public ResponseEntity<ApiResponse<AttendanceResponse>> createAttendance(@Valid @RequestBody AttendanceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                attendanceService.createAttendance(request),
                "Attendance created successfully"
        ));
    }
}
