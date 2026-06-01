package com.school.erp.controller;

import com.school.erp.api.ApiResponse;
import com.school.erp.api.PaginationMeta;
import com.school.erp.dto.student.StudentRequest;
import com.school.erp.dto.student.StudentResponse;
import com.school.erp.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students")
@Tag(name = "Student APIs")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping
    @GetMapping
    @Operation(summary = "List Students", description = "Returns student records with optional class and status filtering")
    public ResponseEntity<ApiResponse<List<StudentResponse>>> getAllStudents(
            @RequestParam(required = false) Long schoolId,
            @RequestParam(required = false) Long classId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id,asc") String sort
    ) {
        Pageable pageable = PageRequest.of(page, size, parseSort(sort));
        Page<StudentResponse> studentPage = studentService.getAllStudents(schoolId, classId, status, pageable);
        return ResponseEntity.ok(ApiResponse.success(
                studentPage.getContent(),
                "Students fetched successfully",
                new PaginationMeta(studentPage.getNumber(), studentPage.getSize(), studentPage.getTotalElements())
        ));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get Student by ID", description = "Returns a student by its database identifier")
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
    @Operation(summary = "Create Student", description = "Creates a new student")
    public ResponseEntity<ApiResponse<StudentResponse>> createStudent(@Valid @RequestBody StudentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                studentService.createStudent(request),
                "Student created successfully"
        ));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update Student", description = "Updates an existing student record")
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
    @Operation(summary = "Deactivate Student", description = "Soft deletes a student by setting status to INACTIVE")
    public ResponseEntity<ApiResponse<Void>> deleteStudent(
            @PathVariable Long id,
            @RequestParam(required = false) Long schoolId
    ) {
        studentService.deleteStudent(id, schoolId);
        return ResponseEntity.ok(ApiResponse.success(null, "Student deactivated successfully"));
    }

    @GetMapping("/search")
    @Operation(summary = "Search Students", description = "Searches active students by name within the current school")
    public ResponseEntity<ApiResponse<List<StudentResponse>>> searchStudents(
            @RequestParam String name,
            @RequestParam(required = false) Long schoolId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                studentService.searchStudents(name, schoolId),
                "Students matching the search criteria fetched successfully"
        ));
    }

    @GetMapping("/admission/{admissionNo}")
    @Operation(summary = "Get Student by Admission Number", description = "Fetches a student by admission number within the current school")
    public ResponseEntity<ApiResponse<StudentResponse>> getStudentByAdmissionNo(
            @PathVariable String admissionNo,
            @RequestParam(required = false) Long schoolId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                studentService.getStudentByAdmissionNo(admissionNo, schoolId),
                "Student fetched successfully by admission number"
        ));
    }

    @GetMapping("/paginated")
    @Operation(summary = "Get Students Paginated", description = "Returns active students in paginated form for the current school")
    public ResponseEntity<ApiResponse<Page<StudentResponse>>> getStudentsPaginated(
            @RequestParam(required = false) Long schoolId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id,asc") String sort
    ) {
        Pageable pageable = PageRequest.of(page, size, parseSort(sort));
        Page<StudentResponse> studentPage = studentService.getStudents(schoolId, pageable);
        return ResponseEntity.ok(ApiResponse.success(
                studentPage,
                "Students fetched successfully",
                new PaginationMeta(studentPage.getNumber(), studentPage.getSize(), studentPage.getTotalElements())
        ));
    }

    private Sort parseSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return Sort.by("id").ascending();
        }
        String[] parts = sort.split(",");
        if (parts.length == 1) {
            return Sort.by(parts[0].trim()).ascending();
        }
        String property = parts[0].trim();
        String direction = parts[1].trim().toLowerCase();
        return direction.equals("desc") ? Sort.by(property).descending() : Sort.by(property).ascending();
    }
}
