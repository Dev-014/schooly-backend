package com.school.erp.controller;

import com.school.erp.api.ApiResponse;
import com.school.erp.dto.staff.StaffRequest;
import com.school.erp.dto.staff.StaffResponse;
import com.school.erp.service.StaffService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/staff")
public class StaffController {

    private final StaffService staffService;

    public StaffController(StaffService staffService) {
        this.staffService = staffService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<StaffResponse>>> getAllStaff(@RequestParam(required = false) Long schoolId) {
        return ResponseEntity.ok(ApiResponse.success(
                staffService.getAllStaff(schoolId),
                "Staff fetched successfully"
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<StaffResponse>> getStaffById(
            @PathVariable Long id,
            @RequestParam(required = false) Long schoolId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                staffService.getStaffById(id, schoolId),
                "Staff fetched successfully"
        ));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<StaffResponse>> createStaff(@Valid @RequestBody StaffRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                staffService.createStaff(request),
                "Staff created successfully"
        ));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<StaffResponse>> updateStaff(
            @PathVariable Long id,
            @RequestParam(required = false) Long schoolId,
            @Valid @RequestBody StaffRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                staffService.updateStaff(id, schoolId, request),
                "Staff updated successfully"
        ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteStaff(
            @PathVariable Long id,
            @RequestParam(required = false) Long schoolId
    ) {
        staffService.deleteStaff(id, schoolId);
        return ResponseEntity.ok(ApiResponse.success(null, "Staff deleted successfully"));
    }
}
