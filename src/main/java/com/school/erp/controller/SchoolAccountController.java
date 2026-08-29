package com.school.erp.controller;

import com.school.erp.api.ApiResponse;
import com.school.erp.dto.finance.SchoolAccountRequest;
import com.school.erp.dto.finance.SchoolAccountResponse;
import com.school.erp.service.finance.SchoolAccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/finance/accounts")
@RequiredArgsConstructor
public class SchoolAccountController {

    private final SchoolAccountService schoolAccountService;

    @PostMapping
    public ResponseEntity<ApiResponse<SchoolAccountResponse>> createAccount(
            @RequestParam Long schoolId,
            @Valid @RequestBody SchoolAccountRequest request) {
        SchoolAccountResponse response = schoolAccountService.createAccount(schoolId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Account created successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<SchoolAccountResponse>>> getAccounts(
            @RequestParam Long schoolId) {
        List<SchoolAccountResponse> response = schoolAccountService.getAccounts(schoolId);
        return ResponseEntity.ok(ApiResponse.success(response, "Accounts fetched successfully"));
    }

    @PatchMapping("/{id}/toggle-status")
    public ResponseEntity<ApiResponse<SchoolAccountResponse>> toggleStatus(
            @PathVariable Long id,
            @RequestParam Long schoolId) {
        SchoolAccountResponse response = schoolAccountService.toggleActiveStatus(id, schoolId);
        return ResponseEntity.ok(ApiResponse.success(response, "Account status updated successfully"));
    }
}
