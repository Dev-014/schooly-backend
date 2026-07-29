package com.school.erp.controller;

import com.school.erp.api.ApiResponse;
import com.school.erp.dto.student.FamilyRequest;
import com.school.erp.dto.student.FamilyResponse;
import com.school.erp.service.FamilyService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/families", "/api/v1/admin/families"})
public class FamilyController {

    private final FamilyService familyService;

    public FamilyController(FamilyService familyService) {
        this.familyService = familyService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<FamilyResponse>>> getAllFamilies(
            @RequestParam(required = false) Long schoolId
    ) {
        return ResponseEntity.ok(ApiResponse.success(familyService.getAllFamilies(schoolId), "Families retrieved successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FamilyResponse>> getFamilyById(
            @PathVariable Long id,
            @RequestParam(required = false) Long schoolId
    ) {
        return ResponseEntity.ok(ApiResponse.success(familyService.getFamilyById(id, schoolId), "Family retrieved successfully"));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<FamilyResponse>>> searchFamilies(
            @RequestParam String query,
            @RequestParam(required = false) Long schoolId
    ) {
        return ResponseEntity.ok(ApiResponse.success(familyService.searchFamilies(query, schoolId), "Families searched successfully"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<FamilyResponse>> createFamily(
            @Valid @RequestBody FamilyRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(familyService.createFamily(request), "Family created successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<FamilyResponse>> updateFamily(
            @PathVariable Long id,
            @Valid @RequestBody FamilyRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(familyService.updateFamily(id, request), "Family updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteFamily(
            @PathVariable Long id,
            @RequestParam(required = false) Long schoolId
    ) {
        familyService.deleteFamily(id, schoolId);
        return ResponseEntity.ok(ApiResponse.success(null, "Family deleted successfully"));
    }

    @PostMapping("/merge")
    public ResponseEntity<ApiResponse<FamilyResponse>> mergeFamilies(
            @RequestParam Long sourceFamilyId,
            @RequestParam Long targetFamilyId,
            @RequestParam(required = false) Long schoolId
    ) {
        return ResponseEntity.ok(ApiResponse.success(familyService.mergeFamilies(sourceFamilyId, targetFamilyId, schoolId), "Families merged successfully"));
    }

    @PostMapping("/students/{studentId}/link")
    public ResponseEntity<ApiResponse<Void>> linkStudentToFamily(
            @PathVariable Long studentId,
            @RequestParam Long familyId,
            @RequestParam(required = false) Long schoolId
    ) {
        familyService.linkStudentToFamily(studentId, familyId, schoolId);
        return ResponseEntity.ok(ApiResponse.success(null, "Student linked to family successfully"));
    }

    @DeleteMapping("/students/{studentId}/unlink")
    public ResponseEntity<ApiResponse<Void>> unlinkStudentFromFamily(
            @PathVariable Long studentId,
            @RequestParam(required = false) Long schoolId
    ) {
        familyService.unlinkStudentFromFamily(studentId, schoolId);
        return ResponseEntity.ok(ApiResponse.success(null, "Student unlinked from family successfully"));
    }
}
