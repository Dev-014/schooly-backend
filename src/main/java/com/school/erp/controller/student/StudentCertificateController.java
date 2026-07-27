package com.school.erp.controller.student;

import com.school.erp.api.ApiResponse;
import com.school.erp.dto.student.StudentCertificateRequest;
import com.school.erp.dto.student.StudentCertificateResponse;
import com.school.erp.service.StudentCertificateService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/student-certificates")
public class StudentCertificateController {

    private final StudentCertificateService certificateService;

    public StudentCertificateController(StudentCertificateService certificateService) {
        this.certificateService = certificateService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<StudentCertificateResponse>>> getAllCertificates(
            @RequestParam(required = false) Long schoolId
    ) {
        return ResponseEntity.ok(ApiResponse.success(certificateService.getAllCertificates(schoolId), "Student certificates retrieved successfully"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<StudentCertificateResponse>> createCertificate(
            @Valid @RequestBody StudentCertificateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(certificateService.createCertificate(request), "Student certificate created successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<StudentCertificateResponse>> updateCertificate(
            @PathVariable Long id,
            @Valid @RequestBody StudentCertificateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(certificateService.updateCertificate(id, request), "Student certificate updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCertificate(
            @PathVariable Long id,
            @RequestParam(required = false) Long schoolId
    ) {
        certificateService.deleteCertificate(id, schoolId);
        return ResponseEntity.ok(ApiResponse.success(null, "Student certificate deleted successfully"));
    }
}
