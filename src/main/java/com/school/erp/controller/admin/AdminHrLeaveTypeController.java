package com.school.erp.controller.admin;

import com.school.erp.dto.hr.SchoolLeaveTypeDTO;
import com.school.erp.dto.hr.SchoolLeaveTypeRequest;
import com.school.erp.dto.hr.StaffLeaveBalanceDTO;
import com.school.erp.dto.hr.StaffLeaveBalanceUpdateRequest;
import com.school.erp.security.PermissionRequired;
import com.school.erp.service.hr.StaffLeaveService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/v1/admin/schools/{schoolId}/hr", "/api/v1/admin/hr"})
@RequiredArgsConstructor
public class AdminHrLeaveTypeController {

    private final StaffLeaveService leaveService;

    private Long resolveSchoolId(Long pathSchoolId, Long paramSchoolId) {
        Long schoolId = pathSchoolId != null ? pathSchoolId : paramSchoolId;
        if (schoolId == null) {
            throw new IllegalArgumentException("schoolId must be provided either in the path or as a query parameter");
        }
        return schoolId;
    }

    @GetMapping("/leave-types")
    @PermissionRequired("staff_hr.staff_leaves.view")
    public ResponseEntity<List<SchoolLeaveTypeDTO>> getLeaveTypes(
            @PathVariable(value = "schoolId", required = false) Long pathSchoolId,
            @RequestParam(value = "schoolId", required = false) Long paramSchoolId) {
        return ResponseEntity.ok(leaveService.getLeaveTypes(resolveSchoolId(pathSchoolId, paramSchoolId)));
    }

    @PostMapping("/leave-types")
    @PermissionRequired("staff_hr.staff_leaves.view")
    public ResponseEntity<SchoolLeaveTypeDTO> createLeaveType(
            @PathVariable(value = "schoolId", required = false) Long pathSchoolId,
            @RequestParam(value = "schoolId", required = false) Long paramSchoolId,
            @Valid @RequestBody SchoolLeaveTypeRequest request) {
        return ResponseEntity.ok(leaveService.createLeaveType(resolveSchoolId(pathSchoolId, paramSchoolId), request));
    }

    @PutMapping("/leave-types/{id}")
    @PermissionRequired("staff_hr.staff_leaves.view")
    public ResponseEntity<SchoolLeaveTypeDTO> updateLeaveType(
            @PathVariable(value = "schoolId", required = false) Long pathSchoolId,
            @RequestParam(value = "schoolId", required = false) Long paramSchoolId,
            @PathVariable Long id,
            @Valid @RequestBody SchoolLeaveTypeRequest request) {
        return ResponseEntity.ok(leaveService.updateLeaveType(resolveSchoolId(pathSchoolId, paramSchoolId), id, request));
    }

    @DeleteMapping("/leave-types/{id}")
    @PermissionRequired("staff_hr.staff_leaves.view")
    public ResponseEntity<Void> deleteLeaveType(
            @PathVariable(value = "schoolId", required = false) Long pathSchoolId,
            @RequestParam(value = "schoolId", required = false) Long paramSchoolId,
            @PathVariable Long id) {
        leaveService.deleteLeaveType(resolveSchoolId(pathSchoolId, paramSchoolId), id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/staff/{staffId}/leave-balances")
    @PermissionRequired("staff_hr.staff_leaves.view")
    public ResponseEntity<List<StaffLeaveBalanceDTO>> getStaffLeaveBalances(
            @PathVariable(value = "schoolId", required = false) Long pathSchoolId,
            @RequestParam(value = "schoolId", required = false) Long paramSchoolId,
            @PathVariable Long staffId) {
        return ResponseEntity.ok(leaveService.getStaffLeaveBalances(resolveSchoolId(pathSchoolId, paramSchoolId), staffId));
    }

    @PutMapping("/staff/{staffId}/leave-balances")
    @PermissionRequired("staff_hr.staff_leaves.view")
    public ResponseEntity<StaffLeaveBalanceDTO> updateStaffLeaveBalance(
            @PathVariable(value = "schoolId", required = false) Long pathSchoolId,
            @RequestParam(value = "schoolId", required = false) Long paramSchoolId,
            @PathVariable Long staffId,
            @Valid @RequestBody StaffLeaveBalanceUpdateRequest request) {
        return ResponseEntity.ok(leaveService.updateStaffLeaveBalance(resolveSchoolId(pathSchoolId, paramSchoolId), staffId, request));
    }
}
