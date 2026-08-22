package com.school.erp.controller.superadmin;

import com.school.erp.api.ApiResponse;
import com.school.erp.dto.auth.PermissionGroupDto;
import com.school.erp.dto.auth.UpdateRolePermissionsRequest;
import com.school.erp.service.auth.RoleManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/superadmin/roles")
@RequiredArgsConstructor
public class SuperAdminRoleController {

    private final RoleManagementService roleManagementService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<com.school.erp.dto.auth.RoleDto>>> getSystemRoles() {
        return ResponseEntity.ok(ApiResponse.success(roleManagementService.getSystemRoles(), "System roles fetched successfully"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<com.school.erp.dto.auth.RoleDto>> createSystemRole(
            @RequestBody @Valid com.school.erp.dto.auth.CreateRoleRequest request) {
        return ResponseEntity.ok(ApiResponse.success(roleManagementService.createSystemRole(request), "System role created successfully"));
    }

    @GetMapping("/{roleId}/permissions")
    public ResponseEntity<ApiResponse<List<PermissionGroupDto>>> getSystemRolePermissions(
            @PathVariable String roleId) {
        return ResponseEntity.ok(ApiResponse.success(roleManagementService.getRolePermissions(null, roleId), "System role permissions fetched successfully"));
    }

    @PutMapping("/{roleId}/permissions")
    public ResponseEntity<ApiResponse<Void>> updateSystemRolePermissions(
            @PathVariable String roleId,
            @RequestBody @Valid UpdateRolePermissionsRequest request) {
        roleManagementService.updateSystemRolePermissions(roleId, request);
        return ResponseEntity.ok(ApiResponse.success(null, "System role permissions updated successfully"));
    }
}
