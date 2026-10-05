package com.school.erp.controller.admin.frontoffice;

import com.school.erp.api.ApiResponse;
import com.school.erp.api.PaginationMeta;
import com.school.erp.dto.frontoffice.ComplaintRequest;
import com.school.erp.dto.frontoffice.ComplaintResponse;
import com.school.erp.dto.frontoffice.ComplaintStatsResponse;
import com.school.erp.security.PermissionRequired;
import com.school.erp.service.frontoffice.SchoolComplaintService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/v1/admin/front-office/complaints", "/api/v1/front-office/complaints"})
@RequiredArgsConstructor
public class AdminComplaintController {

    private final SchoolComplaintService complaintService;

    @GetMapping
    @PermissionRequired("front_office.complaints.view")
    public ResponseEntity<ApiResponse<List<ComplaintResponse>>> getComplaints(
            @RequestParam(required = false) Long schoolId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String complaintType,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<ComplaintResponse> result = complaintService.filterComplaints(
                schoolId, search, complaintType, status, PageRequest.of(page, size));

        return ResponseEntity.ok(ApiResponse.success(
                result.getContent(),
                "Complaints retrieved successfully",
                new PaginationMeta(result.getNumber(), result.getSize(), result.getTotalElements())));
    }

    @GetMapping("/{id}")
    @PermissionRequired("front_office.complaints.view")
    public ResponseEntity<ApiResponse<ComplaintResponse>> getComplaintById(
            @RequestParam(required = false) Long schoolId,
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                complaintService.getComplaintById(schoolId, id),
                "Complaint details retrieved successfully"));
    }

    @PostMapping
    @PermissionRequired("front_office.complaints.view")
    public ResponseEntity<ApiResponse<ComplaintResponse>> createComplaint(
            @RequestParam(required = false) Long schoolId,
            @Valid @RequestBody ComplaintRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                complaintService.createComplaint(schoolId, request),
                "Complaint registered successfully"));
    }

    @PutMapping("/{id}")
    @PermissionRequired("front_office.complaints.view")
    public ResponseEntity<ApiResponse<ComplaintResponse>> updateComplaint(
            @RequestParam(required = false) Long schoolId,
            @PathVariable Long id,
            @Valid @RequestBody ComplaintRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                complaintService.updateComplaint(schoolId, id, request),
                "Complaint updated successfully"));
    }

    @PutMapping("/{id}/status")
    @PermissionRequired("front_office.complaints.view")
    public ResponseEntity<ApiResponse<ComplaintResponse>> updateStatus(
            @RequestParam(required = false) Long schoolId,
            @PathVariable Long id,
            @RequestParam String status,
            @RequestParam(required = false) String actionTaken) {
        return ResponseEntity.ok(ApiResponse.success(
                complaintService.updateComplaintStatus(schoolId, id, status, actionTaken),
                "Complaint status updated successfully"));
    }

    @GetMapping("/stats")
    @PermissionRequired("front_office.complaints.view")
    public ResponseEntity<ApiResponse<ComplaintStatsResponse>> getStats(
            @RequestParam(required = false) Long schoolId) {
        return ResponseEntity.ok(ApiResponse.success(
                complaintService.getStats(schoolId),
                "Complaint statistics retrieved successfully"));
    }

    @DeleteMapping("/{id}")
    @PermissionRequired("front_office.complaints.view")
    public ResponseEntity<ApiResponse<Void>> deleteComplaint(
            @RequestParam(required = false) Long schoolId,
            @PathVariable Long id) {
        complaintService.deleteComplaint(schoolId, id);
        return ResponseEntity.ok(ApiResponse.success(null, "Complaint deleted successfully"));
    }
}
