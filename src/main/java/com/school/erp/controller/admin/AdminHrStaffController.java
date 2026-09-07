package com.school.erp.controller.admin;

import com.school.erp.dto.staff.StaffRequest;
import com.school.erp.dto.auth.UserAssignmentRequest;
import com.school.erp.dto.auth.UserAssignmentResponse;
import com.school.erp.entity.Staff;
import com.school.erp.security.PermissionRequired;
import com.school.erp.service.hr.HrStaffService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/hr/staff")
@RequiredArgsConstructor
public class AdminHrStaffController {

    private final HrStaffService hrStaffService;

    @GetMapping
    @PermissionRequired("staff_hr.staff_directory.view")
    public ResponseEntity<List<Staff>> getAllStaff(
            @RequestParam("schoolId") Long schoolId) {
        return ResponseEntity.ok(hrStaffService.getStaffBySchool(schoolId));
    }

    @GetMapping("/{id}")
    @PermissionRequired("staff_hr.staff_directory.view")
    public ResponseEntity<Staff> getStaffById(
            @RequestParam("schoolId") Long schoolId,
            @PathVariable Long id) {
        return ResponseEntity.ok(hrStaffService.getStaffById(schoolId, id));
    }

    @PostMapping
    @PermissionRequired("staff_hr.staff_directory.view")
    public ResponseEntity<Staff> createStaff(
            @RequestParam("schoolId") Long schoolId,
            @Valid @RequestBody StaffRequest request) {
        return ResponseEntity.ok(hrStaffService.createStaff(schoolId, request));
    }

    @PutMapping("/{id}")
    @PermissionRequired("staff_hr.staff_directory.view")
    public ResponseEntity<Staff> updateStaff(
            @RequestParam("schoolId") Long schoolId,
            @PathVariable Long id,
            @Valid @RequestBody StaffRequest request) {
        return ResponseEntity.ok(hrStaffService.updateStaff(schoolId, id, request));
    }

    @GetMapping("/{id}/assignments")
    @PermissionRequired("staff_hr.staff_directory.view")
    public ResponseEntity<List<UserAssignmentResponse>> getStaffAssignments(
            @RequestParam("schoolId") Long schoolId,
            @PathVariable Long id) {
        return ResponseEntity.ok(hrStaffService.getStaffAssignments(schoolId, id));
    }

    @PostMapping("/{id}/assignments")
    @PermissionRequired("staff_hr.staff_directory.view")
    public ResponseEntity<UserAssignmentResponse> assignRole(
            @RequestParam("schoolId") Long schoolId,
            @PathVariable Long id,
            @Valid @RequestBody UserAssignmentRequest request) {
        return ResponseEntity.ok(hrStaffService.assignRole(schoolId, id, request));
    }

    @DeleteMapping("/{id}/assignments/{assignmentId}")
    @PermissionRequired("staff_hr.staff_directory.view")
    public ResponseEntity<Void> revokeAssignment(
            @RequestParam("schoolId") Long schoolId,
            @PathVariable Long id,
            @PathVariable Long assignmentId) {
        hrStaffService.revokeAssignment(schoolId, id, assignmentId);
        return ResponseEntity.noContent().build();
    }
}
