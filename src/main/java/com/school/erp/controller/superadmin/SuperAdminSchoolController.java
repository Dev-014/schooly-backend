package com.school.erp.controller.superadmin;

import com.school.erp.api.ApiResponse;
import com.school.erp.dto.superadmin.SchoolDto;
import com.school.erp.service.superadmin.SuperAdminSchoolService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/super-admin/schools", "/api/v1/super-admin/schools"})
public class SuperAdminSchoolController {

    private final SuperAdminSchoolService schoolService;

    public SuperAdminSchoolController(SuperAdminSchoolService schoolService) {
        this.schoolService = schoolService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<SchoolDto>>> getAllSchools(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String plan,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(ApiResponse.success(schoolService.getAllSchools(status, plan, search), "Schools fetched successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SchoolDto>> getSchool(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(schoolService.getSchool(id), "School fetched successfully"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SchoolDto>> createSchool(@Valid @RequestBody SchoolDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(schoolService.createSchool(dto), "School created successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SchoolDto>> updateSchool(@PathVariable Long id, @Valid @RequestBody SchoolDto dto) {
        return ResponseEntity.ok(ApiResponse.success(schoolService.updateSchool(id, dto), "School updated successfully"));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<SchoolDto>> updateStatus(@PathVariable Long id, @RequestBody SchoolDto dto) {
        return ResponseEntity.ok(ApiResponse.success(schoolService.updateStatus(id, dto.getStatus()), "School status updated"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSchool(@PathVariable Long id) {
        schoolService.deleteSchool(id);
        return ResponseEntity.ok(ApiResponse.success(null, "School deleted successfully"));
    }
}
