package com.school.erp.controller;

import com.school.erp.api.ApiResponse;
import com.school.erp.dto.student.StudentRequest;
import com.school.erp.dto.student.StudentResponse;
import com.school.erp.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<StudentResponse>>> getAllStudents(
            @RequestParam(required = false) Long schoolId,
            @RequestParam(required = false) Long classId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                studentService.getAllStudents(schoolId, classId),
                "Students fetched successfully"
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<StudentResponse>> getStudentById(
            @PathVariable Long id,
            @RequestParam(required = false) Long schoolId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                studentService.getStudentById(id, schoolId),
                "Student fetched successfully"
        ));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<StudentResponse>> createStudent(@Valid @RequestBody StudentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                studentService.createStudent(request),
                "Student created successfully"
        ));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<StudentResponse>> updateStudent(
            @PathVariable Long id,
            @RequestParam(required = false) Long schoolId,
            @Valid @RequestBody StudentRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                studentService.updateStudent(id, schoolId, request),
                "Student updated successfully"
        ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteStudent(
            @PathVariable Long id,
            @RequestParam(required = false) Long schoolId
    ) {
        studentService.deleteStudent(id, schoolId);
        return ResponseEntity.ok(ApiResponse.success(null, "Student deactivated successfully"));
    }
}
