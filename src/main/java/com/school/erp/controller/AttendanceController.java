package com.school.erp.controller;

import com.school.erp.api.ApiResponse;
import com.school.erp.dto.attendance.AttendanceRequest;
import com.school.erp.dto.attendance.AttendanceResponse;
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

    @PostMapping
    public ResponseEntity<ApiResponse<AttendanceResponse>> createAttendance(@Valid @RequestBody AttendanceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                attendanceService.createAttendance(request),
                "Attendance created successfully"
        ));
    }
}
