package com.school.erp.controller;

import com.school.erp.api.ApiResponse;
import com.school.erp.dto.school.SchoolRequest;
import com.school.erp.dto.school.SchoolResponse;
import com.school.erp.service.SchoolService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/schools")
public class SchoolController {

    private final SchoolService schoolService;

    public SchoolController(SchoolService schoolService) {
        this.schoolService = schoolService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<SchoolResponse>>> getAllSchools() {
        return ResponseEntity.ok(ApiResponse.success(
                schoolService.getAllSchools(),
                "Schools fetched successfully"
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SchoolResponse>> getSchoolById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                schoolService.getSchoolById(id),
                "School fetched successfully"
        ));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SchoolResponse>> createSchool(@Valid @RequestBody SchoolRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                schoolService.createSchool(request),
                "School created successfully"
        ));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SchoolResponse>> updateSchool(@PathVariable Long id, @Valid @RequestBody SchoolRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                schoolService.updateSchool(id, request),
                "School updated successfully"
        ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSchool(@PathVariable Long id) {
        schoolService.deleteSchool(id);
        return ResponseEntity.ok(ApiResponse.success(null, "School deleted successfully"));
    }
}
