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
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String approverComments,
            @RequestBody(required = false) java.util.Map<String, Object> body) {
        String finalStatus = status;
        if ((finalStatus == null || finalStatus.isBlank()) && body != null && body.containsKey("status")) {
            finalStatus = String.valueOf(body.get("status"));
        }
        if (finalStatus == null || finalStatus.isBlank()) {
            finalStatus = "APPROVED";
        }

        Long finalAdminStaffId = adminStaffId;
        if (finalAdminStaffId == null && body != null && body.containsKey("adminStaffId")) {
            try {
                finalAdminStaffId = Long.valueOf(String.valueOf(body.get("adminStaffId")));
            } catch (Exception ignored) {}
        }

        String finalComments = approverComments;
        if (finalComments == null && body != null && body.containsKey("approverComments")) {
            finalComments = String.valueOf(body.get("approverComments"));
        }
        return ResponseEntity.ok(leaveService.approveLeave(resolveSchoolId(pathSchoolId, paramSchoolId), leaveId, finalAdminStaffId, finalStatus, finalComments));
    }

    @GetMapping
    public ResponseEntity<List<StaffLeaveDTO>> getAllLeaves(
            @PathVariable(value = "schoolId", required = false) Long pathSchoolId,
            @RequestParam(value = "schoolId", required = false) Long paramSchoolId) {
        return ResponseEntity.ok(leaveService.getAllLeaves(resolveSchoolId(pathSchoolId, paramSchoolId)));
    }
}

