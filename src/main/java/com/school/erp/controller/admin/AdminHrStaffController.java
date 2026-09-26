package com.school.erp.controller.admin;

import com.school.erp.dto.auth.UserAssignmentRequest;
import com.school.erp.dto.auth.UserAssignmentResponse;
import com.school.erp.dto.hr.StaffBankAccountDTO;
import com.school.erp.dto.hr.StaffBankAccountRequest;
import com.school.erp.dto.hr.StaffDocumentDTO;
import com.school.erp.dto.hr.StaffDocumentRequest;
import com.school.erp.dto.staff.StaffRequest;
import com.school.erp.entity.hr.Staff;
import com.school.erp.security.PermissionRequired;
import com.school.erp.service.hr.HrStaffService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/v1/admin/schools/{schoolId}/hr/staff", "/api/v1/admin/hr/staff"})
@RequiredArgsConstructor
public class AdminHrStaffController {

    private final HrStaffService hrStaffService;

    private Long resolveSchoolId(Long pathSchoolId, Long paramSchoolId) {
        Long schoolId = pathSchoolId != null ? pathSchoolId : paramSchoolId;
        if (schoolId == null) {
            throw new IllegalArgumentException("schoolId must be provided either in the path or as a query parameter");
        }
        return schoolId;
    }

    @GetMapping
    @PermissionRequired("staff_hr.staff_directory.view")
    public ResponseEntity<List<Staff>> getAllStaff(
            @PathVariable(value = "schoolId", required = false) Long pathSchoolId,
            @RequestParam(value = "schoolId", required = false) Long paramSchoolId) {
        return ResponseEntity.ok(hrStaffService.getStaffBySchool(resolveSchoolId(pathSchoolId, paramSchoolId)));
    }

    @GetMapping("/{id}")
    @PermissionRequired("staff_hr.staff_directory.view")
    public ResponseEntity<Staff> getStaffById(
            @PathVariable(value = "schoolId", required = false) Long pathSchoolId,
            @RequestParam(value = "schoolId", required = false) Long paramSchoolId,
            @PathVariable Long id) {
        return ResponseEntity.ok(hrStaffService.getStaffById(resolveSchoolId(pathSchoolId, paramSchoolId), id));
    }

    @PostMapping
    @PermissionRequired("staff_hr.staff_directory.view")
    public ResponseEntity<Staff> createStaff(
            @PathVariable(value = "schoolId", required = false) Long pathSchoolId,
            @RequestParam(value = "schoolId", required = false) Long paramSchoolId,
            @Valid @RequestBody StaffRequest request) {
        return ResponseEntity.ok(hrStaffService.createStaff(resolveSchoolId(pathSchoolId, paramSchoolId), request));
    }

    @PutMapping("/{id}")
    @PermissionRequired("staff_hr.staff_directory.view")
    public ResponseEntity<Staff> updateStaff(
            @PathVariable(value = "schoolId", required = false) Long pathSchoolId,
            @RequestParam(value = "schoolId", required = false) Long paramSchoolId,
            @PathVariable Long id,
            @Valid @RequestBody StaffRequest request) {
        return ResponseEntity.ok(hrStaffService.updateStaff(resolveSchoolId(pathSchoolId, paramSchoolId), id, request));
    }

    @GetMapping("/{id}/assignments")
    @PermissionRequired("staff_hr.staff_directory.view")
    public ResponseEntity<List<UserAssignmentResponse>> getStaffAssignments(
            @PathVariable(value = "schoolId", required = false) Long pathSchoolId,
            @RequestParam(value = "schoolId", required = false) Long paramSchoolId,
            @PathVariable Long id) {
        return ResponseEntity.ok(hrStaffService.getStaffAssignments(resolveSchoolId(pathSchoolId, paramSchoolId), id));
    }

    @PostMapping("/{id}/assignments")
    @PermissionRequired("staff_hr.staff_directory.view")
    public ResponseEntity<UserAssignmentResponse> assignRole(
            @PathVariable(value = "schoolId", required = false) Long pathSchoolId,
            @RequestParam(value = "schoolId", required = false) Long paramSchoolId,
            @PathVariable Long id,
            @Valid @RequestBody UserAssignmentRequest request) {
        return ResponseEntity.ok(hrStaffService.assignRole(resolveSchoolId(pathSchoolId, paramSchoolId), id, request));
    }

    @DeleteMapping("/{id}/assignments/{assignmentId}")
    @PermissionRequired("staff_hr.staff_directory.view")
    public ResponseEntity<Void> revokeAssignment(
            @PathVariable(value = "schoolId", required = false) Long pathSchoolId,
            @RequestParam(value = "schoolId", required = false) Long paramSchoolId,
            @PathVariable Long id,
            @PathVariable Long assignmentId) {
        hrStaffService.revokeAssignment(resolveSchoolId(pathSchoolId, paramSchoolId), id, assignmentId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/documents")
    @PermissionRequired("staff_hr.staff_directory.view")
    public ResponseEntity<List<StaffDocumentDTO>> getStaffDocuments(
            @PathVariable(value = "schoolId", required = false) Long pathSchoolId,
            @RequestParam(value = "schoolId", required = false) Long paramSchoolId,
            @PathVariable Long id) {
        return ResponseEntity.ok(hrStaffService.getStaffDocuments(resolveSchoolId(pathSchoolId, paramSchoolId), id));
    }

    @PostMapping("/{id}/documents")
    @PermissionRequired("staff_hr.staff_directory.view")
    public ResponseEntity<StaffDocumentDTO> addStaffDocument(
            @PathVariable(value = "schoolId", required = false) Long pathSchoolId,
            @RequestParam(value = "schoolId", required = false) Long paramSchoolId,
            @PathVariable Long id,
            @Valid @RequestBody StaffDocumentRequest request) {
        return ResponseEntity.ok(hrStaffService.addStaffDocument(resolveSchoolId(pathSchoolId, paramSchoolId), id, request));
    }

    @DeleteMapping("/{id}/documents/{docId}")
    @PermissionRequired("staff_hr.staff_directory.view")
    public ResponseEntity<Void> deleteStaffDocument(
            @PathVariable(value = "schoolId", required = false) Long pathSchoolId,
            @RequestParam(value = "schoolId", required = false) Long paramSchoolId,
            @PathVariable Long id,
            @PathVariable Long docId) {
        hrStaffService.deleteStaffDocument(resolveSchoolId(pathSchoolId, paramSchoolId), id, docId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/bank-account")
    @PermissionRequired("staff_hr.staff_directory.view")
    public ResponseEntity<StaffBankAccountDTO> getStaffBankAccount(
            @PathVariable(value = "schoolId", required = false) Long pathSchoolId,
            @RequestParam(value = "schoolId", required = false) Long paramSchoolId,
            @PathVariable Long id) {
        return ResponseEntity.ok(hrStaffService.getStaffBankAccount(resolveSchoolId(pathSchoolId, paramSchoolId), id));
    }

    @PutMapping("/{id}/bank-account")
    @PermissionRequired("staff_hr.staff_directory.view")
    public ResponseEntity<StaffBankAccountDTO> updateStaffBankAccount(
            @PathVariable(value = "schoolId", required = false) Long pathSchoolId,
            @RequestParam(value = "schoolId", required = false) Long paramSchoolId,
            @PathVariable Long id,
            @Valid @RequestBody StaffBankAccountRequest request) {
        return ResponseEntity.ok(hrStaffService.updateStaffBankAccount(resolveSchoolId(pathSchoolId, paramSchoolId), id, request));
    }
}
