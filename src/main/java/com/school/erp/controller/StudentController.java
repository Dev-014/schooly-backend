package com.school.erp.controller;

import com.school.erp.api.ApiResponse;
import com.school.erp.dto.student.*;
import com.school.erp.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/api/students", "/api/v1/admin/students"})
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<StudentResponse>>> getAllStudents(
            @RequestParam(required = false) Long schoolId,
            @RequestParam(required = false) Long classId,
            @RequestParam(required = false) Long sectionId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                studentService.getAllStudents(schoolId, classId, sectionId),
                "Students fetched successfully"
        ));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<StudentStatsResponse>> getStudentStats(
            @RequestParam(required = false) Long schoolId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                studentService.getStudentStats(schoolId),
                "Student enrollment stats fetched successfully"
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
                "Student enrolled successfully"
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
                "Student profile updated successfully"
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

    @PostMapping("/promote")
    public ResponseEntity<ApiResponse<Map<String, Object>>> promoteStudents(
            @RequestParam(required = false) Long schoolId,
            @Valid @RequestBody StudentPromotionRequest request
    ) {
        int count = studentService.promoteStudents(schoolId, request);
        return ResponseEntity.ok(ApiResponse.success(
                Map.of("promotedCount", count, "targetClassId", request.targetClassId()),
                "Successfully promoted " + count + " students to target class"
        ));
    }

    @PostMapping("/bulk-import")
    public ResponseEntity<ApiResponse<List<StudentResponse>>> bulkImportStudents(
            @RequestParam(required = false) Long schoolId,
            @RequestBody List<StudentRequest> requests
    ) {
        List<StudentResponse> imported = studentService.bulkImportStudents(schoolId, requests);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                imported,
                "Successfully imported " + imported.size() + " student records"
        ));
    }

    @GetMapping("/{id}/documents")
    public ResponseEntity<ApiResponse<List<StudentDocumentResponse>>> getStudentDocuments(
            @PathVariable Long id,
            @RequestParam(required = false) Long schoolId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                studentService.getStudentDocuments(id, schoolId),
                "Student documents fetched successfully"
        ));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<StudentResponse>>> searchStudents(
            @RequestParam String query,
            @RequestParam(required = false) Long schoolId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                studentService.searchStudents(query, schoolId),
                "Students searched successfully"
        ));
    }

    @PostMapping("/{id}/documents")
    public ResponseEntity<ApiResponse<StudentDocumentResponse>> addStudentDocument(
            @PathVariable Long id,
            @RequestParam(required = false) Long schoolId,
            @Valid @RequestBody StudentDocumentRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                studentService.addStudentDocument(id, schoolId, request),
                "Student document attached successfully"
        ));
    }

    @DeleteMapping("/documents/{docId}")
    public ResponseEntity<ApiResponse<Void>> deleteStudentDocument(
            @PathVariable Long docId,
            @RequestParam(required = false) Long schoolId
    ) {
        studentService.deleteStudentDocument(docId, schoolId);
        return ResponseEntity.ok(ApiResponse.success(null, "Document deleted successfully"));
    }
}
