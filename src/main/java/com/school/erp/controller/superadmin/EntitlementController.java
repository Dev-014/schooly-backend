package com.school.erp.controller;

import com.school.erp.api.ApiResponse;
import com.school.erp.dto.catalog.EntitlementEvaluationDto;
import com.school.erp.service.EntitlementService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/entitlements", "/api/entitlements", "/api/v1/entitlements"})
public class EntitlementController {

    private final EntitlementService entitlementService;

    public EntitlementController(EntitlementService entitlementService) {
        this.entitlementService = entitlementService;
    }

    @GetMapping("/school/{schoolId}")
    public ResponseEntity<ApiResponse<EntitlementEvaluationDto>> getSchoolEntitlements(@PathVariable Long schoolId) {
        return ResponseEntity.ok(ApiResponse.success(
                entitlementService.evaluateEntitlements(schoolId),
                "Entitlements evaluated successfully"
        ));
    }

    @PostMapping("/school/{schoolId}/override")
    public ResponseEntity<ApiResponse<Void>> grantOverride(
            @PathVariable Long schoolId,
            @RequestParam String moduleCode,
            @RequestParam(defaultValue = "ADD_ON") String overrideType,
            @RequestParam(required = false) Integer durationDays) {
        entitlementService.grantOverride(schoolId, moduleCode, overrideType, durationDays);
        return ResponseEntity.ok(ApiResponse.success(null, "Entitlement override granted successfully"));
    }
}
