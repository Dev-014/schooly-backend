package com.school.erp.controller;

import com.school.erp.api.ApiResponse;
import com.school.erp.dto.finance.CollectionPlanRequest;
import com.school.erp.dto.finance.CollectionPlanResponse;
import com.school.erp.service.finance.CollectionPlanService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/finance/collection-plans")
public class CollectionPlanController {

    private final CollectionPlanService collectionPlanService;

    public CollectionPlanController(CollectionPlanService collectionPlanService) {
        this.collectionPlanService = collectionPlanService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CollectionPlanResponse>> createPlan(
            @RequestParam Long schoolId,
            @Valid @RequestBody CollectionPlanRequest request) {
        CollectionPlanResponse response = collectionPlanService.createPlan(schoolId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Collection Plan created successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CollectionPlanResponse>>> getPlans(
            @RequestParam Long schoolId) {
        List<CollectionPlanResponse> response = collectionPlanService.getPlans(schoolId);
        return ResponseEntity.ok(ApiResponse.success(response, "Collection Plans fetched successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CollectionPlanResponse>> updatePlan(
            @PathVariable Long id,
            @RequestParam Long schoolId,
            @Valid @RequestBody CollectionPlanRequest request) {
        CollectionPlanResponse response = collectionPlanService.updatePlan(id, schoolId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Collection Plan updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePlan(
            @PathVariable Long id,
            @RequestParam Long schoolId) {
        collectionPlanService.deletePlan(id, schoolId);
        return ResponseEntity.ok(ApiResponse.success(null, "Collection Plan deleted successfully"));
    }
}
