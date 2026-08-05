package com.school.erp.controller.superadmin;

import com.school.erp.dto.EmployeePayrollDTO;
import com.school.erp.dto.RunPayrollRequest;
import com.school.erp.service.superadmin.EmployeePayrollService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/super-admin/payroll")
@RequiredArgsConstructor
public class EmployeePayrollController {

    private final EmployeePayrollService payrollService;

    @GetMapping
    public ResponseEntity<List<EmployeePayrollDTO>> getAllPayrolls() {
        return ResponseEntity.ok(payrollService.getAllPayrolls());
    }

    @PostMapping("/run")
    public ResponseEntity<EmployeePayrollDTO> runPayroll(@Valid @RequestBody RunPayrollRequest request) {
        return ResponseEntity.ok(payrollService.runPayroll(request));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<EmployeePayrollDTO> updatePayrollStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        return ResponseEntity.ok(payrollService.updatePayrollStatus(id, status));
    }
}
