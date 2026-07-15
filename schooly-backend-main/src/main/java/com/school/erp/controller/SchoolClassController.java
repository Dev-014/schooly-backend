package com.school.erp.controller;

import com.school.erp.api.ApiResponse;
import com.school.erp.dto.schoolclass.SchoolClassRequest;
import com.school.erp.dto.schoolclass.SchoolClassResponse;
import com.school.erp.service.SchoolClassService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/classes")
public class SchoolClassController {

    private final SchoolClassService schoolClassService;

    public SchoolClassController(SchoolClassService schoolClassService) {
        this.schoolClassService = schoolClassService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<SchoolClassResponse>>> getAllClasses(@RequestParam(required = false) Long schoolId) {
        return ResponseEntity.ok(ApiResponse.success(
                schoolClassService.getAllClasses(schoolId),
                "Classes fetched successfully"
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SchoolClassResponse>> getClassById(
            @PathVariable Long id,
            @RequestParam(required = false) Long schoolId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                schoolClassService.getClassById(id, schoolId),
                "Class fetched successfully"
        ));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SchoolClassResponse>> createClass(@Valid @RequestBody SchoolClassRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                schoolClassService.createClass(request),
                "Class created successfully"
        ));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SchoolClassResponse>> updateClass(
            @PathVariable Long id,
            @RequestParam(required = false) Long schoolId,
            @Valid @RequestBody SchoolClassRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                schoolClassService.updateClass(id, schoolId, request),
                "Class updated successfully"
        ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteClass(
            @PathVariable Long id,
            @RequestParam(required = false) Long schoolId
    ) {
        schoolClassService.deleteClass(id, schoolId);
        return ResponseEntity.ok(ApiResponse.success(null, "Class deleted successfully"));
    }
}
