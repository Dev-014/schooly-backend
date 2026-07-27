package com.school.erp.controller.student;

import com.school.erp.api.ApiResponse;
import com.school.erp.dto.student.OnlineAdmissionRequest;
import com.school.erp.dto.student.OnlineAdmissionResponse;
import com.school.erp.service.OnlineAdmissionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/online-admissions")
public class OnlineAdmissionController {

    private final OnlineAdmissionService admissionService;

    public OnlineAdmissionController(OnlineAdmissionService admissionService) {
        this.admissionService = admissionService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<OnlineAdmissionResponse>>> getAdmissions(
            @RequestParam(required = false) Long schoolId
    ) {
        return ResponseEntity.ok(ApiResponse.success(admissionService.getAdmissions(schoolId), "Online admissions retrieved successfully"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<OnlineAdmissionResponse>> createAdmission(
            @Valid @RequestBody OnlineAdmissionRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(admissionService.createAdmission(request), "Online admission created successfully"));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<OnlineAdmissionResponse>> updateStatus(
            @PathVariable Long id,
            @RequestParam(required = false) Long schoolId,
            @RequestBody Map<String, String> body
    ) {
        return ResponseEntity.ok(ApiResponse.success(admissionService.updateStatus(id, schoolId, body.get("status")), "Online admission status updated successfully"));
    }
}
