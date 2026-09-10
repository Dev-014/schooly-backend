package com.school.erp.controller.admin.frontoffice;

import com.school.erp.api.ApiResponse;
import com.school.erp.api.PaginationMeta;
import com.school.erp.dto.frontoffice.ParcelReceiveRequest;
import com.school.erp.dto.frontoffice.ParcelReceiveResponse;
import com.school.erp.dto.frontoffice.ParcelReceiveStatsResponse;
import com.school.erp.security.PermissionRequired;
import com.school.erp.service.frontoffice.ParcelReceiveService;
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
@RequestMapping("/api/v1/admin/front-office/parcels/receive")
@RequiredArgsConstructor
public class AdminParcelReceiveController {

    private final ParcelReceiveService parcelReceiveService;

    @GetMapping
    @PermissionRequired("front_office.parcel_receive.view")
    public ResponseEntity<ApiResponse<List<ParcelReceiveResponse>>> getParcelReceives(
            @RequestParam Long schoolId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<ParcelReceiveResponse> result = parcelReceiveService.filterParcelReceives(
                schoolId, search, date, status, PageRequest.of(page, size));

        return ResponseEntity.ok(ApiResponse.success(
                result.getContent(),
                "Incoming parcel logs retrieved successfully",
                new PaginationMeta(result.getNumber(), result.getSize(), result.getTotalElements())));
    }

    @GetMapping("/{id}")
    @PermissionRequired("front_office.parcel_receive.view")
    public ResponseEntity<ApiResponse<ParcelReceiveResponse>> getParcelReceiveById(
            @RequestParam Long schoolId,
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                parcelReceiveService.getParcelReceiveById(schoolId, id),
                "Parcel receive details retrieved successfully"));
    }

    @PostMapping
    @PermissionRequired("front_office.parcel_receive.view")
    public ResponseEntity<ApiResponse<ParcelReceiveResponse>> receiveParcel(
            @RequestParam Long schoolId,
            @Valid @RequestBody ParcelReceiveRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                parcelReceiveService.receiveParcel(schoolId, request),
                "Parcel logged successfully"));
    }

    @PutMapping("/{id}/collect")
    @PermissionRequired("front_office.parcel_receive.view")
    public ResponseEntity<ApiResponse<ParcelReceiveResponse>> markCollected(
            @RequestParam Long schoolId,
            @PathVariable Long id,
            @RequestParam(required = false) String collectedBy) {
        return ResponseEntity.ok(ApiResponse.success(
                parcelReceiveService.markCollected(schoolId, id, collectedBy),
                "Parcel marked as collected"));
    }

    @GetMapping("/stats")
    @PermissionRequired("front_office.parcel_receive.view")
    public ResponseEntity<ApiResponse<ParcelReceiveStatsResponse>> getStats(
            @RequestParam Long schoolId) {
        return ResponseEntity.ok(ApiResponse.success(
                parcelReceiveService.getParcelReceiveStats(schoolId),
                "Parcel receive statistics retrieved successfully"));
    }

    @DeleteMapping("/{id}")
    @PermissionRequired("front_office.parcel_receive.view")
    public ResponseEntity<ApiResponse<Void>> deleteParcel(
            @RequestParam Long schoolId,
            @PathVariable Long id) {
        parcelReceiveService.deleteParcelReceive(schoolId, id);
        return ResponseEntity.ok(ApiResponse.success(null, "Parcel receive log deleted successfully"));
    }
}
