package com.school.erp.controller.superadmin;

import com.school.erp.api.ApiResponse;
import com.school.erp.dto.user.GrantAccessDTO;
import com.school.erp.dto.user.GrantAccessResponseDTO;
import com.school.erp.service.user.UserAccessService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api/v1/users", "/super-admin/users", "/admin/users"})
@RequiredArgsConstructor
public class UserAccessController {

    private final UserAccessService userAccessService;

    @PostMapping("/grant-access")
    public ResponseEntity<ApiResponse<GrantAccessResponseDTO>> grantAccess(
            @Valid @RequestBody GrantAccessDTO request) {
        return ResponseEntity.ok(ApiResponse.success(
                userAccessService.grantAccess(request),
                "Grant access processed successfully"
        ));
    }

    @GetMapping("/credentials")
    public ResponseEntity<ApiResponse<GrantAccessResponseDTO>> getCredentials(
            @RequestParam String entityType,
            @RequestParam Long entityId) {
        return ResponseEntity.ok(ApiResponse.success(
                userAccessService.getCredentials(entityType, entityId),
                "Credentials fetched successfully"
        ));
    }

    @PostMapping("/credentials/reset")
    public ResponseEntity<ApiResponse<GrantAccessResponseDTO>> resetCredentials(
            @RequestParam String entityType,
            @RequestParam Long entityId) {
        return ResponseEntity.ok(ApiResponse.success(
                userAccessService.resetCredentials(entityType, entityId),
                "Credentials reset successfully"
        ));
    }
}
