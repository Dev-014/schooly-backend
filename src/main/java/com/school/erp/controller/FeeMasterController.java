package com.school.erp.controller;

import com.school.erp.api.ApiResponse;
import com.school.erp.dto.feecategory.FeeCategoryRequest;
import com.school.erp.dto.feecategory.FeeCategoryResponse;
import com.school.erp.dto.feestructure.FeeStructureRequest;
import com.school.erp.dto.feestructure.FeeStructureResponse;
import com.school.erp.service.FeeMasterService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/fee-masters")
public class FeeMasterController {

    private final FeeMasterService feeMasterService;

    public FeeMasterController(FeeMasterService feeMasterService) {
        this.feeMasterService = feeMasterService;
    }

    // --- Categories ---
    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<FeeCategoryResponse>>> getAllCategories(@RequestParam Long schoolId) {
        return ResponseEntity.ok(ApiResponse.success(feeMasterService.getAllCategories(schoolId), "Categories fetched"));
    }

    @PostMapping("/categories")
    public ResponseEntity<ApiResponse<FeeCategoryResponse>> createCategory(@Valid @RequestBody FeeCategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(feeMasterService.createCategory(request), "Category created"));
    }

    @PutMapping("/categories/{id}")
    public ResponseEntity<ApiResponse<FeeCategoryResponse>> updateCategory(@PathVariable Long id, @RequestParam Long schoolId, @Valid @RequestBody FeeCategoryRequest request) {
        return ResponseEntity.ok(ApiResponse.success(feeMasterService.updateCategory(id, schoolId, request), "Category updated"));
    }

    @DeleteMapping("/categories/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable Long id, @RequestParam Long schoolId) {
        feeMasterService.deleteCategory(id, schoolId);
        return ResponseEntity.ok(ApiResponse.success(null, "Category deleted"));
    }

    // --- Structures ---
    @GetMapping("/structures")
    public ResponseEntity<ApiResponse<List<FeeStructureResponse>>> getAllStructures(@RequestParam Long schoolId, @RequestParam(required = false) Long academicYearId) {
        return ResponseEntity.ok(ApiResponse.success(feeMasterService.getAllStructures(schoolId, academicYearId), "Structures fetched"));
    }

    @GetMapping("/structures/{id}")
    public ResponseEntity<ApiResponse<FeeStructureResponse>> getStructureById(@PathVariable Long id, @RequestParam Long schoolId) {
        return ResponseEntity.ok(ApiResponse.success(feeMasterService.getStructureById(id, schoolId), "Structure fetched"));
    }

    @PostMapping("/structures")
    public ResponseEntity<ApiResponse<FeeStructureResponse>> createStructure(@Valid @RequestBody FeeStructureRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(feeMasterService.createStructure(request), "Structure created"));
    }

    @PutMapping("/structures/{id}")
    public ResponseEntity<ApiResponse<FeeStructureResponse>> updateStructure(@PathVariable Long id, @RequestParam Long schoolId, @Valid @RequestBody FeeStructureRequest request) {
        return ResponseEntity.ok(ApiResponse.success(feeMasterService.updateStructure(id, schoolId, request), "Structure updated"));
    }

    @DeleteMapping("/structures/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteStructure(@PathVariable Long id, @RequestParam Long schoolId) {
        feeMasterService.deleteStructure(id, schoolId);
        return ResponseEntity.ok(ApiResponse.success(null, "Structure deleted"));
    }
}
