package com.school.erp.controller.admin;

import com.school.erp.dto.hr.StaffLeaveDTO;
import com.school.erp.dto.hr.StaffLeaveRequest;
import com.school.erp.service.hr.StaffLeaveService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/schools/{schoolId}/hr/leaves")
@RequiredArgsConstructor
public class AdminHrLeaveController {

    private final StaffLeaveService leaveService;

    @PostMapping
    public ResponseEntity<StaffLeaveDTO> applyLeave(
            @PathVariable Long schoolId,
            @Valid @RequestBody StaffLeaveRequest request) {
        return ResponseEntity.ok(leaveService.applyLeave(schoolId, request));
    }

    @PutMapping("/{leaveId}/approve")
    public ResponseEntity<StaffLeaveDTO> approveLeave(
            @PathVariable Long schoolId,
            @PathVariable Long leaveId,
            @RequestParam Long adminStaffId,
            @RequestParam String status) {
        return ResponseEntity.ok(leaveService.approveLeave(schoolId, leaveId, adminStaffId, status));
    }

    @GetMapping
    public ResponseEntity<List<StaffLeaveDTO>> getAllLeaves(@PathVariable Long schoolId) {
        return ResponseEntity.ok(leaveService.getAllLeaves(schoolId));
    }
}
