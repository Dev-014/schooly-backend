package com.school.erp.controller.superadmin;

import com.school.erp.dto.AddPerformanceReviewRequest;
import com.school.erp.dto.EmployeePerformanceDTO;
import com.school.erp.service.superadmin.EmployeePerformanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/super-admin/performance")
@RequiredArgsConstructor
public class EmployeePerformanceController {

    private final EmployeePerformanceService performanceService;

    @GetMapping
    public ResponseEntity<List<EmployeePerformanceDTO>> getAllReviews() {
        return ResponseEntity.ok(performanceService.getAllReviews());
    }

    @PostMapping
    public ResponseEntity<EmployeePerformanceDTO> addReview(@Valid @RequestBody AddPerformanceReviewRequest request) {
        return ResponseEntity.ok(performanceService.addReview(request));
    }
}
