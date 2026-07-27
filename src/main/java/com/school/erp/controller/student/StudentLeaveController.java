package com.school.erp.controller.student;

import com.school.erp.api.ApiResponse;
import com.school.erp.dto.student.StudentLeaveRequest;
import com.school.erp.dto.student.StudentLeaveResponse;
import com.school.erp.service.StudentLeaveService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/student-leaves")
public class StudentLeaveController {

    private final StudentLeaveService leaveService;

    public StudentLeaveController(StudentLeaveService leaveService) {
        this.leaveService = leaveService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<StudentLeaveResponse>>> getAllLeaves(
            @RequestParam(required = false) Long schoolId
    ) {
        return ResponseEntity.ok(ApiResponse.success(leaveService.getAllLeaves(schoolId), "Student leaves retrieved successfully"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<StudentLeaveResponse>> createLeave(
            @Valid @RequestBody StudentLeaveRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(leaveService.createLeave(request), "Student leave created successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<StudentLeaveResponse>> updateLeave(
            @PathVariable Long id,
            @Valid @RequestBody StudentLeaveRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(leaveService.updateLeave(id, request), "Student leave updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteLeave(
            @PathVariable Long id,
            @RequestParam(required = false) Long schoolId
    ) {
        leaveService.deleteLeave(id, schoolId);
        return ResponseEntity.ok(ApiResponse.success(null, "Student leave deleted successfully"));
    }
}
