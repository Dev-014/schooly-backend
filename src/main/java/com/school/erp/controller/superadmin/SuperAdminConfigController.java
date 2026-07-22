package com.school.erp.controller.superadmin;

import com.school.erp.api.ApiResponse;
import com.school.erp.dto.superadmin.SystemConfigDto;
import com.school.erp.service.superadmin.SuperAdminConfigService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/super-admin/config", "/api/v1/super-admin/config"})
public class SuperAdminConfigController {

    private final SuperAdminConfigService configService;

    public SuperAdminConfigController(SuperAdminConfigService configService) {
        this.configService = configService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<SystemConfigDto>> getConfig() {
        return ResponseEntity.ok(ApiResponse.success(configService.getConfig(), "System configuration fetched successfully"));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<SystemConfigDto>> updateConfig(@Valid @RequestBody SystemConfigDto dto) {
        return ResponseEntity.ok(ApiResponse.success(configService.updateConfig(dto), "System configuration updated successfully"));
    }
}
