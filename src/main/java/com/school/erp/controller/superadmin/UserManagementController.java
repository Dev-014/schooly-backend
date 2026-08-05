package com.school.erp.controller.superadmin;

import com.school.erp.dto.usermanagement.*;
import com.school.erp.service.superadmin.UserManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/super-admin/users")
@RequiredArgsConstructor
public class UserManagementController {

    private final UserManagementService userManagementService;

    @GetMapping("/dashboard/stats")
    public ResponseEntity<UserDashboardStatsDTO> getDashboardStats() {
        return ResponseEntity.ok(userManagementService.getDashboardStats());
    }

    @GetMapping("/school/{schoolId}")
    public ResponseEntity<Page<UserDTO>> getSchoolUsers(
            @PathVariable Long schoolId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(userManagementService.getSchoolUsers(schoolId, PageRequest.of(page, size)));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<UserDTO>> searchUsers(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Long schoolId,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(userManagementService.searchUsers(query, schoolId, role, status, PageRequest.of(page, size)));
    }

    @GetMapping("/login-history")
    public ResponseEntity<Page<UserLoginHistoryDTO>> getLoginHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(userManagementService.getLoginHistory(PageRequest.of(page, size)));
    }

    @GetMapping("/activity")
    public ResponseEntity<Page<UserActivityLogDTO>> getActivityLogs(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Long schoolId,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String module,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(userManagementService.getActivityLogs(query, schoolId, role, module, PageRequest.of(page, size)));
    }

    @GetMapping("/requests")
    public ResponseEntity<Page<AccountRequestDTO>> getAccountRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(userManagementService.getAccountRequests(PageRequest.of(page, size)));
    }

    @PutMapping("/requests/{id}/approve")
    public ResponseEntity<Void> approveAccountRequest(@PathVariable Long id) {
        userManagementService.approveAccountRequest(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/requests/{id}/reject")
    public ResponseEntity<Void> rejectAccountRequest(@PathVariable Long id, @RequestParam(required = false) String reason) {
        userManagementService.rejectAccountRequest(id, reason);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{userId}/impersonate")
    public ResponseEntity<ImpersonationResponseDTO> impersonateUser(@PathVariable Long userId) {
        return ResponseEntity.ok(userManagementService.impersonateUser(userId));
    }

    @PostMapping
    public ResponseEntity<UserDTO> createUser(@RequestBody @jakarta.validation.Valid UserRequestDTO request) {
        return ResponseEntity.status(201).body(userManagementService.createUser(request));
    }

    @PutMapping("/{userId}")
    public ResponseEntity<UserDTO> updateUser(@PathVariable Long userId, @RequestBody @jakarta.validation.Valid UserRequestDTO request) {
        return ResponseEntity.ok(userManagementService.updateUser(userId, request));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        userManagementService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{userId}/status")
    public ResponseEntity<Void> updateUserStatus(@PathVariable Long userId, @RequestParam String status) {
        userManagementService.updateUserStatus(userId, status);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{userId}/reset-password")
    public ResponseEntity<java.util.Map<String, String>> resetUserPassword(@PathVariable Long userId) {
        String tempPass = userManagementService.resetUserPassword(userId);
        return ResponseEntity.ok(java.util.Map.of("tempPassword", tempPass));
    }

    @GetMapping("/roles")
    public ResponseEntity<java.util.List<String>> getAvailableRoles() {
        return ResponseEntity.ok(userManagementService.getAvailableRoles());
    }
}
