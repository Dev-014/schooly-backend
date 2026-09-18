package com.school.erp.controller;

import com.school.erp.api.ApiResponse;
import com.school.erp.dto.catalog.*;
import com.school.erp.service.CatalogService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping({"/catalog", "/api/catalog", "/api/v1/catalog"})
public class CatalogController {

    private final CatalogService catalogService;

    public CatalogController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping("/plans")
    public ResponseEntity<ApiResponse<List<CatalogPlanDto>>> getPlans() {
        return ResponseEntity.ok(ApiResponse.success(catalogService.getActivePlans(), "Plans fetched successfully"));
    }

    @GetMapping("/modules")
    public ResponseEntity<ApiResponse<List<CatalogModuleDto>>> getModules() {
        return ResponseEntity.ok(ApiResponse.success(catalogService.getActiveModules(), "Modules fetched successfully"));
    }

    @GetMapping("/roles")
    public ResponseEntity<ApiResponse<List<CatalogRoleDto>>> getRoles() {
        return ResponseEntity.ok(ApiResponse.success(catalogService.getSystemRoles(), "Roles fetched successfully"));
    }

    @GetMapping("/academic-cycles")
    public ResponseEntity<ApiResponse<List<CatalogOptionDto>>> getAcademicCycles() {
        return ResponseEntity.ok(ApiResponse.success(catalogService.getAcademicCycles(), "Academic cycles fetched successfully"));
    }

    @GetMapping("/grade-levels")
    public ResponseEntity<ApiResponse<List<CatalogOptionDto>>> getGradeLevels() {
        return ResponseEntity.ok(ApiResponse.success(catalogService.getGradeLevels(), "Grade levels fetched successfully"));
    }
}
