package com.school.erp.controller.admin.frontoffice;

import com.school.erp.api.ApiResponse;
import com.school.erp.api.PaginationMeta;
import com.school.erp.dto.frontoffice.VisitorLogRequest;
import com.school.erp.dto.frontoffice.VisitorLogResponse;
import com.school.erp.dto.frontoffice.VisitorStatsResponse;
import com.school.erp.security.PermissionRequired;
import com.school.erp.service.frontoffice.VisitorLogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/front-office/visitors")
@RequiredArgsConstructor
public class AdminVisitorBookController {

    private final VisitorLogService visitorLogService;

    @GetMapping
    @PermissionRequired("front_office.visitor_book.view")
    public ResponseEntity<ApiResponse<List<VisitorLogResponse>>> getVisitors(
            @RequestParam Long schoolId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String purpose,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<VisitorLogResponse> result = visitorLogService.filterVisitors(
                schoolId, search, date, purpose, status, PageRequest.of(page, size));

        return ResponseEntity.ok(ApiResponse.success(
                result.getContent(),
                "Visitor logs retrieved successfully",
                new PaginationMeta(result.getNumber(), result.getSize(), result.getTotalElements())));
    }

    @GetMapping("/{id}")
    @PermissionRequired("front_office.visitor_book.view")
    public ResponseEntity<ApiResponse<VisitorLogResponse>> getVisitorById(
            @RequestParam Long schoolId,
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                visitorLogService.getVisitorById(schoolId, id),
                "Visitor log details retrieved successfully"));
    }

    @PostMapping
    @PermissionRequired("front_office.visitor_book.view")
    public ResponseEntity<ApiResponse<VisitorLogResponse>> createVisitor(
            @RequestParam Long schoolId,
            @Valid @RequestBody VisitorLogRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                visitorLogService.createVisitor(schoolId, request),
                "Visitor entry recorded successfully"));
    }

    @PutMapping("/{id}/checkout")
    @PermissionRequired("front_office.visitor_book.view")
    public ResponseEntity<ApiResponse<VisitorLogResponse>> checkoutVisitor(
            @RequestParam Long schoolId,
            @PathVariable Long id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime timeOut) {
        return ResponseEntity.ok(ApiResponse.success(
                visitorLogService.checkoutVisitor(schoolId, id, timeOut),
                "Visitor checked out successfully"));
    }

    @GetMapping("/stats")
    @PermissionRequired("front_office.visitor_book.view")
    public ResponseEntity<ApiResponse<VisitorStatsResponse>> getStats(
            @RequestParam Long schoolId) {
        return ResponseEntity.ok(ApiResponse.success(
                visitorLogService.getVisitorStats(schoolId),
                "Visitor statistics retrieved successfully"));
    }

    @DeleteMapping("/{id}")
    @PermissionRequired("front_office.visitor_book.view")
    public ResponseEntity<ApiResponse<Void>> deleteVisitor(
            @RequestParam Long schoolId,
            @PathVariable Long id) {
        visitorLogService.deleteVisitor(schoolId, id);
        return ResponseEntity.ok(ApiResponse.success(null, "Visitor log deleted successfully"));
    }
}
