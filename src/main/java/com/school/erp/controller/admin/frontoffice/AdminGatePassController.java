package com.school.erp.controller.admin.frontoffice;

import com.school.erp.api.ApiResponse;
import com.school.erp.api.PaginationMeta;
import com.school.erp.dto.frontoffice.GatePassRequest;
import com.school.erp.dto.frontoffice.GatePassResponse;
import com.school.erp.dto.frontoffice.GatePassStatsResponse;
import com.school.erp.security.PermissionRequired;
import com.school.erp.service.frontoffice.GatePassService;
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
@RequestMapping("/api/v1/admin/front-office/gate-passes")
@RequiredArgsConstructor
public class AdminGatePassController {

    private final GatePassService gatePassService;

    @GetMapping
    @PermissionRequired("front_office.gate_pass.view")
    public ResponseEntity<ApiResponse<List<GatePassResponse>>> getGatePasses(
            @RequestParam(required = false) Long schoolId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<GatePassResponse> result = gatePassService.filterGatePasses(
                schoolId, search, date, role, status, PageRequest.of(page, size));

        return ResponseEntity.ok(ApiResponse.success(
                result.getContent(),
                "Gate pass logs retrieved successfully",
                new PaginationMeta(result.getNumber(), result.getSize(), result.getTotalElements())));
    }

    @GetMapping("/{id}")
    @PermissionRequired("front_office.gate_pass.view")
    public ResponseEntity<ApiResponse<GatePassResponse>> getGatePassById(
            @RequestParam(required = false) Long schoolId,
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                gatePassService.getGatePassById(schoolId, id),
                "Gate pass details retrieved successfully"));
    }

    @PostMapping
    @PermissionRequired("front_office.gate_pass.view")
    public ResponseEntity<ApiResponse<GatePassResponse>> createGatePass(
            @RequestParam(required = false) Long schoolId,
            @Valid @RequestBody GatePassRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                gatePassService.createGatePass(schoolId, request),
                "Gate pass issued successfully"));
    }

    @PutMapping("/{id}/status")
    @PermissionRequired("front_office.gate_pass.view")
    public ResponseEntity<ApiResponse<GatePassResponse>> updateStatus(
            @RequestParam(required = false) Long schoolId,
            @PathVariable Long id,
            @RequestParam String status) {
        return ResponseEntity.ok(ApiResponse.success(
                gatePassService.updateStatus(schoolId, id, status),
                "Gate pass status updated successfully"));
    }

    @GetMapping("/stats")
    @PermissionRequired("front_office.gate_pass.view")
    public ResponseEntity<ApiResponse<GatePassStatsResponse>> getStats(
            @RequestParam(required = false) Long schoolId) {
        return ResponseEntity.ok(ApiResponse.success(
                gatePassService.getGatePassStats(schoolId),
                "Gate pass statistics retrieved successfully"));
    }

    @DeleteMapping("/{id}")
    @PermissionRequired("front_office.gate_pass.view")
    public ResponseEntity<ApiResponse<Void>> deleteGatePass(
            @RequestParam(required = false) Long schoolId,
            @PathVariable Long id) {
        gatePassService.deleteGatePass(schoolId, id);
        return ResponseEntity.ok(ApiResponse.success(null, "Gate pass deleted successfully"));
    }
}

