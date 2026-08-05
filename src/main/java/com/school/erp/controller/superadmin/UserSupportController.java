package com.school.erp.controller.superadmin;

import com.school.erp.api.ApiResponse;
import com.school.erp.dto.superadmin.PasswordResetDTO;
import com.school.erp.dto.superadmin.UserRequestDTO;
import com.school.erp.service.superadmin.UserSupportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/super-admin/users/support", "/api/v1/super-admin/users/support"})
@RequiredArgsConstructor
public class UserSupportController {

    private final UserSupportService userSupportService;

    @PostMapping("/password-reset/{userId}")
    public ResponseEntity<ApiResponse<PasswordResetDTO>> sendPasswordReset(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success(
                userSupportService.sendPasswordResetLink(userId), 
                "Password reset link sent successfully"));
    }

    @GetMapping("/password-resets")
    public ResponseEntity<ApiResponse<List<PasswordResetDTO>>> getPasswordResets() {
        return ResponseEntity.ok(ApiResponse.success(
                userSupportService.getAllPasswordResets(), 
                "Password resets fetched successfully"));
    }

    @GetMapping("/requests")
    public ResponseEntity<ApiResponse<List<UserRequestDTO>>> getUserRequests(
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(ApiResponse.success(
                userSupportService.getAllUserRequests(status), 
                "User requests fetched successfully"));
    }

    @PostMapping("/requests/{id}/approve")
    public ResponseEntity<ApiResponse<UserRequestDTO>> approveUserRequest(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                userSupportService.approveUserRequest(id), 
                "User request approved successfully"));
    }

    @PostMapping("/requests/{id}/reject")
    public ResponseEntity<ApiResponse<UserRequestDTO>> rejectUserRequest(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body) {
        String notes = body != null ? body.get("notes") : null;
        return ResponseEntity.ok(ApiResponse.success(
                userSupportService.rejectUserRequest(id, notes), 
                "User request rejected successfully"));
    }
}
