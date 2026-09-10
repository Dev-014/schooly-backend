package com.school.erp.controller.admin.frontoffice;

import com.school.erp.api.ApiResponse;
import com.school.erp.api.PaginationMeta;
import com.school.erp.dto.frontoffice.ParcelDispatchRequest;
import com.school.erp.dto.frontoffice.ParcelDispatchResponse;
import com.school.erp.dto.frontoffice.ParcelDispatchStatsResponse;
import com.school.erp.security.PermissionRequired;
import com.school.erp.service.frontoffice.ParcelDispatchService;
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
@RequestMapping("/api/v1/admin/front-office/parcels/dispatch")
@RequiredArgsConstructor
public class AdminParcelDispatchController {

    private final ParcelDispatchService parcelDispatchService;

    @GetMapping
    @PermissionRequired("front_office.parcel_dispatch.view")
    public ResponseEntity<ApiResponse<List<ParcelDispatchResponse>>> getParcelDispatches(
            @RequestParam(required = false) Long schoolId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<ParcelDispatchResponse> result = parcelDispatchService.filterParcelDispatches(
                schoolId, search, date, status, PageRequest.of(page, size));

        return ResponseEntity.ok(ApiResponse.success(
                result.getContent(),
                "Outbound parcel dispatches retrieved successfully",
                new PaginationMeta(result.getNumber(), result.getSize(), result.getTotalElements())));
    }

    @GetMapping("/{id}")
    @PermissionRequired("front_office.parcel_dispatch.view")
    public ResponseEntity<ApiResponse<ParcelDispatchResponse>> getParcelDispatchById(
            @RequestParam(required = false) Long schoolId,
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                parcelDispatchService.getParcelDispatchById(schoolId, id),
                "Parcel dispatch details retrieved successfully"));
    }

    @PostMapping
    @PermissionRequired("front_office.parcel_dispatch.view")
    public ResponseEntity<ApiResponse<ParcelDispatchResponse>> createParcelDispatch(
            @RequestParam(required = false) Long schoolId,
            @Valid @RequestBody ParcelDispatchRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                parcelDispatchService.createParcelDispatch(schoolId, request),
                "Parcel dispatch recorded successfully"));
    }

    @PutMapping("/{id}/status")
    @PermissionRequired("front_office.parcel_dispatch.view")
    public ResponseEntity<ApiResponse<ParcelDispatchResponse>> updateStatus(
            @RequestParam(required = false) Long schoolId,
            @PathVariable Long id,
            @RequestParam String status) {
        return ResponseEntity.ok(ApiResponse.success(
                parcelDispatchService.updateStatus(schoolId, id, status),
                "Parcel dispatch status updated successfully"));
    }

    @GetMapping("/stats")
    @PermissionRequired("front_office.parcel_dispatch.view")
    public ResponseEntity<ApiResponse<ParcelDispatchStatsResponse>> getStats(
            @RequestParam(required = false) Long schoolId) {
        return ResponseEntity.ok(ApiResponse.success(
                parcelDispatchService.getParcelDispatchStats(schoolId),
                "Parcel dispatch statistics retrieved successfully"));
    }

    @DeleteMapping("/{id}")
    @PermissionRequired("front_office.parcel_dispatch.view")
    public ResponseEntity<ApiResponse<Void>> deleteParcelDispatch(
            @RequestParam(required = false) Long schoolId,
            @PathVariable Long id) {
        parcelDispatchService.deleteParcelDispatch(schoolId, id);
        return ResponseEntity.ok(ApiResponse.success(null, "Parcel dispatch record deleted successfully"));
    }
}

