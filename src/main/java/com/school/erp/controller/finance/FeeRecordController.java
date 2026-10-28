package com.school.erp.controller.finance;

import com.school.erp.api.ApiResponse;
import com.school.erp.dto.payment.FeeStatsDTO;
import com.school.erp.dto.payment.StudentFeeRecordDTO;
import com.school.erp.security.PermissionRequired;
import com.school.erp.service.finance.FeeRecordService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/finance/fee-records")
public class FeeRecordController {

    private final FeeRecordService feeRecordService;

    public FeeRecordController(FeeRecordService feeRecordService) {
        this.feeRecordService = feeRecordService;
    }

    @GetMapping
    @PermissionRequired("fees.search_due_fees.view")
    public ResponseEntity<ApiResponse<Page<StudentFeeRecordDTO>>> getFeeRecords(
            @RequestParam Long schoolId,
            @RequestParam(required = false) Long classGroupId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        Page<StudentFeeRecordDTO> records = feeRecordService.getStudentFeeRecords(schoolId, classGroupId, search, status, PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.success(records, "Fee records fetched successfully"));
    }

    @GetMapping("/stats")
    @PermissionRequired("fees.search_due_fees.view")
    public ResponseEntity<ApiResponse<FeeStatsDTO>> getFeeStats(
            @RequestParam Long schoolId,
            @RequestParam(required = false) Long classGroupId) {
        return ResponseEntity.ok(ApiResponse.success(feeRecordService.getFeeStats(schoolId, classGroupId), "Fee stats fetched successfully"));
    }
}
