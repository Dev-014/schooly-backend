package com.school.erp.controller.admin;

import com.school.erp.dto.hr.*;
import com.school.erp.security.PermissionRequired;
import com.school.erp.service.hr.StaffPayrollService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/api/v1/admin/schools/{schoolId}/hr/payroll", "/api/v1/admin/hr/payroll"})
@RequiredArgsConstructor
public class AdminHrPayrollController {

    private final StaffPayrollService payrollService;

    private Long resolveSchoolId(Long pathSchoolId, Long paramSchoolId) {
        Long schoolId = pathSchoolId != null ? pathSchoolId : paramSchoolId;
        if (schoolId == null) {
            throw new IllegalArgumentException("schoolId must be provided either in the path or as a query parameter");
        }
        return schoolId;
    }

    @PostMapping
    @PermissionRequired("staff_hr.payroll.view")
    public ResponseEntity<StaffPayrollDTO> runPayroll(
            @PathVariable(value = "schoolId", required = false) Long pathSchoolId,
            @RequestParam(value = "schoolId", required = false) Long paramSchoolId,
            @Valid @RequestBody StaffPayrollRequest request) {
        return ResponseEntity.ok(payrollService.runPayroll(resolveSchoolId(pathSchoolId, paramSchoolId), request));
    }

    @GetMapping
    @PermissionRequired("staff_hr.payroll.view")
    public ResponseEntity<List<StaffPayrollDTO>> getAllPayrolls(
            @PathVariable(value = "schoolId", required = false) Long pathSchoolId,
            @RequestParam(value = "schoolId", required = false) Long paramSchoolId) {
        return ResponseEntity.ok(payrollService.getAllPayrolls(resolveSchoolId(pathSchoolId, paramSchoolId)));
    }

    @PostMapping("/generate-batch")
    @PermissionRequired("staff_hr.payroll.view")
    public ResponseEntity<List<StaffPayrollDTO>> generateBatchPayroll(
            @PathVariable(value = "schoolId", required = false) Long pathSchoolId,
            @RequestParam(value = "schoolId", required = false) Long paramSchoolId,
            @RequestParam(required = false) String payrollMonth,
            @RequestParam(required = false) Integer payrollYear,
            @RequestBody(required = false) StaffPayrollBatchRequest request) {
        String month = request != null && request.getPayrollMonth() != null ? request.getPayrollMonth() : payrollMonth;
        Integer year = request != null && request.getPayrollYear() != null ? request.getPayrollYear() : payrollYear;
        if (month == null || year == null) {
            throw new IllegalArgumentException("Both payrollMonth and payrollYear are required");
        }
        return ResponseEntity.ok(payrollService.generateBatchPayroll(resolveSchoolId(pathSchoolId, paramSchoolId), month, year));
    }

    @GetMapping("/summary")
    @PermissionRequired("staff_hr.payroll.view")
    public ResponseEntity<StaffPayrollSummaryDTO> getPayrollSummary(
            @PathVariable(value = "schoolId", required = false) Long pathSchoolId,
            @RequestParam(value = "schoolId", required = false) Long paramSchoolId,
            @RequestParam(required = false) String month,
            @RequestParam(required = false) Integer year) {
        return ResponseEntity.ok(payrollService.getPayrollSummary(resolveSchoolId(pathSchoolId, paramSchoolId), month, year));
    }

    @GetMapping("/{id}/payslip")
    @PermissionRequired("staff_hr.payroll.view")
    public ResponseEntity<StaffPayslipDTO> getPayslip(
            @PathVariable(value = "schoolId", required = false) Long pathSchoolId,
            @RequestParam(value = "schoolId", required = false) Long paramSchoolId,
            @PathVariable Long id) {
        return ResponseEntity.ok(payrollService.getPayslip(resolveSchoolId(pathSchoolId, paramSchoolId), id));
    }

    @PatchMapping("/{id}/status")
    @PermissionRequired("staff_hr.payroll.view")
    public ResponseEntity<StaffPayrollDTO> updatePayrollStatus(
            @PathVariable(value = "schoolId", required = false) Long pathSchoolId,
            @RequestParam(value = "schoolId", required = false) Long paramSchoolId,
            @PathVariable Long id,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate paymentDate,
            @RequestBody(required = false) Map<String, Object> body) {
        String effectiveStatus = status;
        LocalDate effectiveDate = paymentDate;

        if (body != null) {
            if ((effectiveStatus == null || effectiveStatus.isEmpty()) && body.containsKey("status")) {
                effectiveStatus = (String) body.get("status");
            }
            if (effectiveDate == null && body.containsKey("paymentDate")) {
                effectiveDate = LocalDate.parse(body.get("paymentDate").toString());
            }
        }

        return ResponseEntity.ok(payrollService.updatePayrollStatus(resolveSchoolId(pathSchoolId, paramSchoolId), id, effectiveStatus, effectiveDate));
    }
}
