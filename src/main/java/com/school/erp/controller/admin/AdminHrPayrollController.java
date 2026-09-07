package com.school.erp.controller.admin;

import com.school.erp.dto.hr.StaffPayrollDTO;
import com.school.erp.dto.hr.StaffPayrollRequest;
import com.school.erp.service.hr.StaffPayrollService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/schools/{schoolId}/hr/payroll")
@RequiredArgsConstructor
public class AdminHrPayrollController {

    private final StaffPayrollService payrollService;

    @PostMapping
    public ResponseEntity<StaffPayrollDTO> runPayroll(
            @PathVariable Long schoolId,
            @Valid @RequestBody StaffPayrollRequest request) {
        return ResponseEntity.ok(payrollService.runPayroll(schoolId, request));
    }

    @GetMapping
    public ResponseEntity<List<StaffPayrollDTO>> getAllPayrolls(@PathVariable Long schoolId) {
        return ResponseEntity.ok(payrollService.getAllPayrolls(schoolId));
    }
}
