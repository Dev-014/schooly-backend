package com.school.erp.controller.auth;

import com.school.erp.api.ApiResponse;
import com.school.erp.dto.auth.*;
import com.school.erp.service.auth.RoleManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/roles", "/api/v1/roles"})
@RequiredArgsConstructor
public class RoleController {

    private final RoleManagementService roleManagementService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<RoleDto>>> getRoles(
            @RequestParam(required = false) Long schoolId) {
        if (schoolId == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("schoolId is required"));
        }
        return ResponseEntity.ok(ApiResponse.success(roleManagementService.getRoles(schoolId), "Roles fetched successfully"));
    }

    @GetMapping("/{roleId}/permissions")
    public ResponseEntity<ApiResponse<List<PermissionGroupDto>>> getRolePermissions(
            @RequestParam(required = false) Long schoolId,
            @PathVariable String roleId) {
        if (schoolId == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("schoolId is required"));
        }
        return ResponseEntity.ok(ApiResponse.success(roleManagementService.getRolePermissions(schoolId, roleId), "Role permissions fetched successfully"));
    }

    @PutMapping("/{roleId}/permissions")
    public ResponseEntity<ApiResponse<Void>> updateRolePermissions(
            @RequestParam(required = false) Long schoolId,
            @PathVariable String roleId,
            @RequestBody @Valid UpdateRolePermissionsRequest request) {
        if (schoolId == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("schoolId is required"));
        }
        roleManagementService.updateRolePermissions(schoolId, roleId, request);
        return ResponseEntity.ok(ApiResponse.success(null, "Role permissions updated successfully"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<RoleDto>> createRole(
            @RequestParam(required = false) Long schoolId,
            @RequestBody @Valid CreateRoleRequest request) {
        if (schoolId == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("schoolId is required"));
        }
        return ResponseEntity.ok(ApiResponse.success(roleManagementService.createRole(schoolId, request), "Role created successfully"));
    }
}
