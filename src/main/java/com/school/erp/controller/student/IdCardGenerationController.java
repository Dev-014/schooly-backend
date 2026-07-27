package com.school.erp.controller.student;

import com.school.erp.api.ApiResponse;
import com.school.erp.dto.student.IdCardGenerationRequest;
import com.school.erp.dto.student.IdCardGenerationResponse;
import com.school.erp.service.IdCardGenerationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/student-id-cards")
public class IdCardGenerationController {

    private final IdCardGenerationService idCardService;

    public IdCardGenerationController(IdCardGenerationService idCardService) {
        this.idCardService = idCardService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<IdCardGenerationResponse>>> getAllGenerations(
            @RequestParam(required = false) Long schoolId
    ) {
        return ResponseEntity.ok(ApiResponse.success(idCardService.getAllGenerations(schoolId), "ID card generations retrieved successfully"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<IdCardGenerationResponse>> generateOrUpdateIdCard(
            @Valid @RequestBody IdCardGenerationRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(idCardService.generateOrUpdateIdCard(request), "ID card generated successfully"));
    }
}
