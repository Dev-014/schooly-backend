package com.school.erp.controller.student;

import com.school.erp.api.ApiResponse;
import com.school.erp.dto.student.StudentSiblingRequest;
import com.school.erp.dto.student.StudentSiblingResponse;
import com.school.erp.service.StudentSiblingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/student-siblings")
public class StudentSiblingController {

    private final StudentSiblingService siblingService;

    public StudentSiblingController(StudentSiblingService siblingService) {
        this.siblingService = siblingService;
    }

    @GetMapping("/{primaryStudentId}")
    public ResponseEntity<ApiResponse<List<StudentSiblingResponse>>> getSiblings(
            @PathVariable Long primaryStudentId,
            @RequestParam(required = false) Long schoolId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                siblingService.getSiblings(primaryStudentId, schoolId),
                "Siblings retrieved successfully"
        ));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<StudentSiblingResponse>> linkSibling(
            @Valid @RequestBody StudentSiblingRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                siblingService.linkSibling(request),
                "Sibling linked successfully"
        ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> unlinkSibling(
            @PathVariable Long id,
            @RequestParam(required = false) Long schoolId
    ) {
        siblingService.unlinkSibling(id, schoolId);
        return ResponseEntity.ok(ApiResponse.success(null, "Sibling unlinked successfully"));
    }
}
