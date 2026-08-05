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
@RequestMapping({"/api/v1/users/grant-access", "/super-admin/users/grant-access", "/admin/users/grant-access"})
@RequiredArgsConstructor
public class UserAccessController {

    private final UserAccessService userAccessService;

    @PostMapping
    public ResponseEntity<ApiResponse<GrantAccessResponseDTO>> grantAccess(
            @Valid @RequestBody GrantAccessDTO request) {
        return ResponseEntity.ok(ApiResponse.success(
                userAccessService.grantAccess(request),
                "Grant access processed successfully"
        ));
    }
}
