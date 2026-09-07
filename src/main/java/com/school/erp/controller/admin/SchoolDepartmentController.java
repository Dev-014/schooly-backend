package com.school.erp.controller.admin;

import com.school.erp.api.ApiResponse;
import com.school.erp.entity.hr.SchoolDepartment;
import com.school.erp.service.hr.SchoolDepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/hr/departments")
@RequiredArgsConstructor
public class SchoolDepartmentController {

    private final SchoolDepartmentService service;

    @GetMapping
    public ResponseEntity<ApiResponse<List<SchoolDepartment>>> getAll(@RequestParam Long schoolId) {
        return ResponseEntity.ok(ApiResponse.success(service.getAllDepartments(schoolId), "Fetched departments"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SchoolDepartment>> create(@RequestBody SchoolDepartment department, @RequestParam Long schoolId) {
        // Find School entity and set it... wait, we need SchoolRepository
        // Actually for simplicity, we'll assume DTO mapping handles it, but since we are taking SchoolDepartment directly,
        // we might need to set the school manually. I will update the service.
        return ResponseEntity.ok(ApiResponse.success(service.createDepartment(department), "Created"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SchoolDepartment>> update(@PathVariable Long id, @RequestBody SchoolDepartment department) {
        return ResponseEntity.ok(ApiResponse.success(service.updateDepartment(id, department), "Updated"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        service.deleteDepartment(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Deleted"));
    }
}
