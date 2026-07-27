package com.school.erp.controller.student;

import com.school.erp.api.ApiResponse;
import com.school.erp.dto.student.StudentCategoryRequest;
import com.school.erp.dto.student.StudentCategoryResponse;
import com.school.erp.service.StudentCategoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/student-categories")
public class StudentCategoryController {

    private final StudentCategoryService categoryService;

    public StudentCategoryController(StudentCategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<StudentCategoryResponse>>> getAllCategories(
            @RequestParam(required = false) Long schoolId
    ) {
        return ResponseEntity.ok(ApiResponse.success(categoryService.getAllCategories(schoolId), "Student categories retrieved successfully"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<StudentCategoryResponse>> createCategory(
            @Valid @RequestBody StudentCategoryRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(categoryService.createCategory(request), "Student category created successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<StudentCategoryResponse>> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody StudentCategoryRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(categoryService.updateCategory(id, request), "Student category updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(
            @PathVariable Long id,
            @RequestParam(required = false) Long schoolId
    ) {
        categoryService.deleteCategory(id, schoolId);
        return ResponseEntity.ok(ApiResponse.success(null, "Student category deleted successfully"));
    }
}

