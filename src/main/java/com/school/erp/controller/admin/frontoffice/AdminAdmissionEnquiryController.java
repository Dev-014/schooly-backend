package com.school.erp.controller.admin.frontoffice;

import com.school.erp.api.ApiResponse;
import com.school.erp.api.PaginationMeta;
import com.school.erp.dto.frontoffice.*;
import com.school.erp.security.PermissionRequired;
import com.school.erp.service.frontoffice.AdmissionEnquiryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/front-office/enquiries")
@RequiredArgsConstructor
public class AdminAdmissionEnquiryController {

    private final AdmissionEnquiryService enquiryService;

    @GetMapping
    @PermissionRequired("front_office.admission_enquiry.view")
    public ResponseEntity<ApiResponse<List<AdmissionEnquiryResponse>>> getEnquiries(
            @RequestParam Long schoolId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String source,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<AdmissionEnquiryResponse> result = enquiryService.filterEnquiries(
                schoolId, search, startDate, endDate, source, status, PageRequest.of(page, size));

        return ResponseEntity.ok(ApiResponse.success(
                result.getContent(),
                "Admission enquiries retrieved successfully",
                new PaginationMeta(result.getNumber(), result.getSize(), result.getTotalElements())));
    }

    @GetMapping("/{id}")
    @PermissionRequired("front_office.admission_enquiry.view")
    public ResponseEntity<ApiResponse<AdmissionEnquiryResponse>> getEnquiryById(
            @RequestParam Long schoolId,
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                enquiryService.getEnquiryById(schoolId, id),
                "Enquiry details retrieved successfully"));
    }

    @PostMapping
    @PermissionRequired("front_office.admission_enquiry.view")
    public ResponseEntity<ApiResponse<AdmissionEnquiryResponse>> createEnquiry(
            @RequestParam Long schoolId,
            @Valid @RequestBody AdmissionEnquiryRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                enquiryService.createEnquiry(schoolId, request),
                "Admission enquiry created successfully"));
    }

    @PutMapping("/{id}")
    @PermissionRequired("front_office.admission_enquiry.view")
    public ResponseEntity<ApiResponse<AdmissionEnquiryResponse>> updateEnquiry(
            @RequestParam Long schoolId,
            @PathVariable Long id,
            @Valid @RequestBody AdmissionEnquiryRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                enquiryService.updateEnquiry(schoolId, id, request),
                "Admission enquiry updated successfully"));
    }

    @PostMapping("/{id}/follow-ups")
    @PermissionRequired("front_office.admission_enquiry.view")
    public ResponseEntity<ApiResponse<EnquiryFollowUpResponse>> addFollowUp(
            @RequestParam Long schoolId,
            @PathVariable Long id,
            @Valid @RequestBody EnquiryFollowUpRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                enquiryService.addFollowUp(schoolId, id, request),
                "Follow-up added successfully"));
    }

    @GetMapping("/stats")
    @PermissionRequired("front_office.admission_enquiry.view")
    public ResponseEntity<ApiResponse<AdmissionEnquiryStatsResponse>> getStats(
            @RequestParam Long schoolId) {
        return ResponseEntity.ok(ApiResponse.success(
                enquiryService.getEnquiryStats(schoolId),
                "Admission enquiry statistics retrieved successfully"));
    }

    @DeleteMapping("/{id}")
    @PermissionRequired("front_office.admission_enquiry.view")
    public ResponseEntity<ApiResponse<Void>> deleteEnquiry(
            @RequestParam Long schoolId,
            @PathVariable Long id) {
        enquiryService.deleteEnquiry(schoolId, id);
        return ResponseEntity.ok(ApiResponse.success(null, "Admission enquiry deleted successfully"));
    }
}
