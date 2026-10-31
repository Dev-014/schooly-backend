package com.school.erp.controller.admin;

import com.school.erp.dto.hr.StaffLeaveDTO;
import com.school.erp.dto.hr.StaffLeaveRequest;
import com.school.erp.entity.auth.UserRole;
import com.school.erp.security.PermissionRequired;
import com.school.erp.service.hr.StaffLeaveService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/v1/admin/schools/{schoolId}/hr/leaves", "/api/v1/admin/hr/leaves"})
@RequiredArgsConstructor
public class AdminHrLeaveController {

    private final StaffLeaveService leaveService;

    private Long resolveSchoolId(Long pathSchoolId, Long paramSchoolId) {
        Long schoolId = pathSchoolId != null ? pathSchoolId : paramSchoolId;
        if (schoolId == null) {
            throw new IllegalArgumentException("schoolId must be provided either in the path or as a query parameter");
        }
        return schoolId;
    }

    @PostMapping
    public ResponseEntity<StaffLeaveDTO> applyLeave(
            @PathVariable(value = "schoolId", required = false) Long pathSchoolId,
            @RequestParam(value = "schoolId", required = false) Long paramSchoolId,
            @Valid @RequestBody StaffLeaveRequest request) {
        return ResponseEntity.ok(leaveService.applyLeave(resolveSchoolId(pathSchoolId, paramSchoolId), request));
    }

    @PutMapping("/{leaveId}/approve")
    @PermissionRequired("leave_management.staff_leaves.edit")
    public ResponseEntity<StaffLeaveDTO> approveLeave(
            @PathVariable(value = "schoolId", required = false) Long pathSchoolId,
            @RequestParam(value = "schoolId", required = false) Long paramSchoolId,
            @PathVariable Long leaveId,
            @RequestParam(required = false) Long adminStaffId,
            @RequestParam String status) {
        return ResponseEntity.ok(leaveService.approveLeave(resolveSchoolId(pathSchoolId, paramSchoolId), leaveId, adminStaffId, status));
    }

    @GetMapping
    public ResponseEntity<List<StaffLeaveDTO>> getAllLeaves(
            @PathVariable(value = "schoolId", required = false) Long pathSchoolId,
            @RequestParam(value = "schoolId", required = false) Long paramSchoolId) {
        return ResponseEntity.ok(leaveService.getAllLeaves(resolveSchoolId(pathSchoolId, paramSchoolId)));
    }
}

