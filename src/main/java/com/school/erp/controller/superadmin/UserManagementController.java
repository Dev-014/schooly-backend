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
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(userManagementService.searchUsers(query, PageRequest.of(page, size)));
    }

    @GetMapping("/login-history")
    public ResponseEntity<Page<UserLoginHistoryDTO>> getLoginHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(userManagementService.getLoginHistory(PageRequest.of(page, size)));
    }

    @GetMapping("/activity")
    public ResponseEntity<Page<UserActivityLogDTO>> getActivityLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(userManagementService.getActivityLogs(PageRequest.of(page, size)));
    }

    @GetMapping("/requests")
    public ResponseEntity<Page<AccountRequestDTO>> getAccountRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(userManagementService.getAccountRequests(PageRequest.of(page, size)));
    }
}
