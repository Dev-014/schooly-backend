package com.school.erp.controller.admin;

import com.school.erp.dto.hr.BulkStaffAttendanceRequest;
import com.school.erp.dto.hr.StaffAttendanceDTO;
import com.school.erp.dto.hr.StaffAttendanceRequest;
import com.school.erp.dto.hr.StaffAttendanceStatsDTO;
import com.school.erp.security.PermissionRequired;
import com.school.erp.service.hr.StaffAttendanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping({"/api/v1/admin/schools/{schoolId}/hr/attendance", "/api/v1/admin/hr/attendance"})
@RequiredArgsConstructor
public class AdminHrAttendanceController {

    private final StaffAttendanceService attendanceService;

    @GetMapping("/register")
    @PermissionRequired("staff_hr.staff_attendance.view")
    public ResponseEntity<List<StaffAttendanceDTO>> getAttendanceRegister(
            @PathVariable(value = "schoolId", required = false) Long pathSchoolId,
            @RequestParam(value = "schoolId", required = false) Long paramSchoolId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) String search) {
        Long effectiveSchoolId = pathSchoolId != null ? pathSchoolId : paramSchoolId;
        return ResponseEntity.ok(attendanceService.getAttendanceRegister(effectiveSchoolId, date, departmentId, search));
    }

    @GetMapping("/stats")
    @PermissionRequired("staff_hr.staff_attendance.view")
    public ResponseEntity<StaffAttendanceStatsDTO> getAttendanceStats(
            @PathVariable(value = "schoolId", required = false) Long pathSchoolId,
            @RequestParam(value = "schoolId", required = false) Long paramSchoolId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        Long effectiveSchoolId = pathSchoolId != null ? pathSchoolId : paramSchoolId;
        return ResponseEntity.ok(attendanceService.getAttendanceStats(effectiveSchoolId, date));
    }

    @PostMapping("/bulk")
    @PermissionRequired("staff_hr.staff_attendance.view")
    public ResponseEntity<List<StaffAttendanceDTO>> bulkMarkAttendance(
            @PathVariable(value = "schoolId", required = false) Long pathSchoolId,
            @RequestParam(value = "schoolId", required = false) Long paramSchoolId,
            @Valid @RequestBody BulkStaffAttendanceRequest request) {
        Long effectiveSchoolId = pathSchoolId != null ? pathSchoolId : paramSchoolId;
        return ResponseEntity.ok(attendanceService.bulkMarkAttendance(effectiveSchoolId, request));
    }

    @PostMapping
    @PermissionRequired("staff_hr.staff_attendance.view")
    public ResponseEntity<StaffAttendanceDTO> markAttendance(
            @PathVariable(value = "schoolId", required = false) Long pathSchoolId,
            @RequestParam(value = "schoolId", required = false) Long paramSchoolId,
            @Valid @RequestBody StaffAttendanceRequest request) {
        Long effectiveSchoolId = pathSchoolId != null ? pathSchoolId : paramSchoolId;
        return ResponseEntity.ok(attendanceService.markAttendance(effectiveSchoolId, request));
    }

    @GetMapping
    @PermissionRequired("staff_hr.staff_attendance.view")
    public ResponseEntity<List<StaffAttendanceDTO>> getAttendanceByDate(
            @PathVariable(value = "schoolId", required = false) Long pathSchoolId,
            @RequestParam(value = "schoolId", required = false) Long paramSchoolId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        Long effectiveSchoolId = pathSchoolId != null ? pathSchoolId : paramSchoolId;
        return ResponseEntity.ok(attendanceService.getAttendanceByDate(effectiveSchoolId, date));
    }

    @GetMapping("/staff/{staffId}")
    @PermissionRequired("staff_hr.staff_attendance.view")
    public ResponseEntity<com.school.erp.dto.hr.StaffMonthlyAttendanceBreakdownDTO> getStaffMonthlyAttendance(
            @PathVariable(value = "schoolId", required = false) Long pathSchoolId,
            @RequestParam(value = "schoolId", required = false) Long paramSchoolId,
            @PathVariable Long staffId,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        Long effectiveSchoolId = pathSchoolId != null ? pathSchoolId : paramSchoolId;
        return ResponseEntity.ok(attendanceService.getStaffMonthlyAttendance(effectiveSchoolId, staffId, year, month));
    }

    @GetMapping("/report")
    @PermissionRequired("staff_hr.staff_attendance.view")
    public ResponseEntity<com.school.erp.dto.hr.StaffAttendanceReportDTO> getAttendanceMonthlyReport(
            @PathVariable(value = "schoolId", required = false) Long pathSchoolId,
            @RequestParam(value = "schoolId", required = false) Long paramSchoolId,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Long departmentId) {
        Long effectiveSchoolId = pathSchoolId != null ? pathSchoolId : paramSchoolId;
        return ResponseEntity.ok(attendanceService.getAttendanceMonthlyReport(effectiveSchoolId, year, month, departmentId));
    }
}
