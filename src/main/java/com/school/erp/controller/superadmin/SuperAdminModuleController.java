package com.school.erp.controller.superadmin;

import com.school.erp.api.ApiResponse;
import com.school.erp.dto.superadmin.ModuleDto;
import com.school.erp.service.superadmin.SuperAdminModuleService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/super-admin/modules", "/api/v1/super-admin/modules"})
public class SuperAdminModuleController {

    private final SuperAdminModuleService moduleService;

    public SuperAdminModuleController(SuperAdminModuleService moduleService) {
        this.moduleService = moduleService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ModuleDto>>> getAllModules() {
        return ResponseEntity.ok(ApiResponse.success(moduleService.getAllModules(), "Modules fetched successfully"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ModuleDto>> createModule(@Valid @RequestBody ModuleDto dto) {
        return ResponseEntity.ok(ApiResponse.success(moduleService.createModule(dto), "Module created successfully"));
    }

    @PostMapping("/school/{schoolId}/toggle")
    public ResponseEntity<ApiResponse<Void>> toggleModuleForSchool(
            @PathVariable Long schoolId,
            @RequestParam String moduleCode,
            @RequestParam boolean enabled) {
        moduleService.toggleModuleForSchool(schoolId, moduleCode, enabled);
        return ResponseEntity.ok(ApiResponse.success(null, "Module toggle applied"));
    }
}
