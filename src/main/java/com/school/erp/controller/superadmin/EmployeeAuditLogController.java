package com.school.erp.controller.superadmin;

import com.school.erp.dto.EmployeeAuditLogDTO;
import com.school.erp.service.superadmin.EmployeeAuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/super-admin/audit-logs")
@RequiredArgsConstructor
public class EmployeeAuditLogController {

    private final EmployeeAuditLogService auditLogService;

    @GetMapping
    public ResponseEntity<List<EmployeeAuditLogDTO>> getAllAuditLogs() {
        return ResponseEntity.ok(auditLogService.getAllAuditLogs());
    }
}
