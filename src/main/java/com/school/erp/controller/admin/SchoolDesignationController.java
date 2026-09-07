package com.school.erp.controller.admin;

import com.school.erp.api.ApiResponse;
import com.school.erp.entity.hr.SchoolDesignation;
import com.school.erp.service.hr.SchoolDesignationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/hr/designations")
@RequiredArgsConstructor
public class SchoolDesignationController {

    private final SchoolDesignationService service;

    @GetMapping
    public ResponseEntity<ApiResponse<List<SchoolDesignation>>> getAll(@RequestParam Long schoolId) {
        return ResponseEntity.ok(ApiResponse.success(service.getAllDesignations(schoolId), "Fetched designations"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SchoolDesignation>> create(@RequestBody SchoolDesignation designation, @RequestParam Long schoolId) {
        return ResponseEntity.ok(ApiResponse.success(service.createDesignation(designation), "Created"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SchoolDesignation>> update(@PathVariable Long id, @RequestBody SchoolDesignation designation) {
        return ResponseEntity.ok(ApiResponse.success(service.updateDesignation(id, designation), "Updated"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        service.deleteDesignation(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Deleted"));
    }
}
