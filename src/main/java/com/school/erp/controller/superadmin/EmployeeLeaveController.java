package com.school.erp.controller.superadmin;

import com.school.erp.dto.ApplyLeaveRequest;
import com.school.erp.dto.EmployeeLeaveDTO;
import com.school.erp.service.superadmin.EmployeeLeaveService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/super-admin/leaves")
@RequiredArgsConstructor
public class EmployeeLeaveController {

    private final EmployeeLeaveService leaveService;

    @GetMapping
    public ResponseEntity<List<EmployeeLeaveDTO>> getAllLeaves() {
        return ResponseEntity.ok(leaveService.getAllLeaves());
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<EmployeeLeaveDTO>> getLeavesByEmployee(@PathVariable Long employeeId) {
        return ResponseEntity.ok(leaveService.getLeavesByEmployee(employeeId));
    }

    @PostMapping
    public ResponseEntity<EmployeeLeaveDTO> applyLeave(@Valid @RequestBody ApplyLeaveRequest request) {
        return ResponseEntity.ok(leaveService.applyLeave(request));
    }

    @PutMapping("/{leaveId}/status")
    public ResponseEntity<EmployeeLeaveDTO> updateLeaveStatus(
            @PathVariable Long leaveId,
            @RequestParam String status,
            @RequestParam Long approvedById) {
        return ResponseEntity.ok(leaveService.updateLeaveStatus(leaveId, status, approvedById));
    }
}
