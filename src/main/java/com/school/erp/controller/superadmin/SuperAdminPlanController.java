package com.school.erp.controller.superadmin;

import com.school.erp.api.ApiResponse;
import com.school.erp.dto.superadmin.PlanDto;
import com.school.erp.service.superadmin.SuperAdminPlanService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/super-admin/plans", "/api/v1/super-admin/plans"})
public class SuperAdminPlanController {

    private final SuperAdminPlanService planService;

    public SuperAdminPlanController(SuperAdminPlanService planService) {
        this.planService = planService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PlanDto>>> getAllPlans() {
        return ResponseEntity.ok(ApiResponse.success(planService.getAllPlans(), "Subscription plans fetched successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PlanDto>> getPlan(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(planService.getPlan(id), "Subscription plan fetched successfully"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PlanDto>> createPlan(@Valid @RequestBody PlanDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(planService.createPlan(dto), "Subscription plan created successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PlanDto>> updatePlan(@PathVariable Long id, @Valid @RequestBody PlanDto dto) {
        return ResponseEntity.ok(ApiResponse.success(planService.updatePlan(id, dto), "Subscription plan updated successfully"));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<PlanDto>> updateStatus(@PathVariable Long id, @RequestBody PlanDto dto) {
        return ResponseEntity.ok(ApiResponse.success(planService.updateStatus(id, dto.getStatus()), "Subscription plan status updated"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePlan(@PathVariable Long id) {
        planService.deletePlan(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Subscription plan deleted successfully"));
    }
}
