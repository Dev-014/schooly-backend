package com.school.erp.controller.admin;

import com.school.erp.dto.hr.StaffAttendanceDTO;
import com.school.erp.dto.hr.StaffAttendanceRequest;
import com.school.erp.service.hr.StaffAttendanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/schools/{schoolId}/hr/attendance")
@RequiredArgsConstructor
public class AdminHrAttendanceController {

    private final StaffAttendanceService attendanceService;

    @PostMapping
    public ResponseEntity<StaffAttendanceDTO> markAttendance(
            @PathVariable Long schoolId,
            @Valid @RequestBody StaffAttendanceRequest request) {
        return ResponseEntity.ok(attendanceService.markAttendance(schoolId, request));
    }

    @GetMapping
    public ResponseEntity<List<StaffAttendanceDTO>> getAttendanceByDate(
            @PathVariable Long schoolId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(attendanceService.getAttendanceByDate(schoolId, date));
    }
}
