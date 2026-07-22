package com.school.erp.controller.superadmin;

import com.school.erp.api.ApiResponse;
import com.school.erp.dto.superadmin.SuperAdminLogEntryDto;
import com.school.erp.dto.superadmin.SuperAdminLogStatsDto;
import com.school.erp.service.superadmin.SuperAdminLogService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/super-admin/logs", "/api/v1/super-admin/logs"})
public class SuperAdminLogController {

    private final SuperAdminLogService logService;

    public SuperAdminLogController(SuperAdminLogService logService) {
        this.logService = logService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<SuperAdminLogEntryDto>>> getLogs(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size) {
        return ResponseEntity.ok(ApiResponse.success(logService.getLogs(status, search, page, size), "System logs fetched successfully"));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<SuperAdminLogStatsDto>> getStats() {
        return ResponseEntity.ok(ApiResponse.success(logService.getStats(), "Log monitoring statistics fetched successfully"));
    }
}
