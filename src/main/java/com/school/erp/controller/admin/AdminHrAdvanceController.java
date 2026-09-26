package com.school.erp.controller.admin;

import com.school.erp.dto.hr.StaffAdvanceDTO;
import com.school.erp.dto.hr.StaffAdvanceRequest;
import com.school.erp.security.PermissionRequired;
import com.school.erp.service.hr.StaffPayrollService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/api/v1/admin/schools/{schoolId}/hr/advances", "/api/v1/admin/hr/advances"})
@RequiredArgsConstructor
public class AdminHrAdvanceController {

    private final StaffPayrollService payrollService;

    private Long resolveSchoolId(Long pathSchoolId, Long paramSchoolId) {
        Long schoolId = pathSchoolId != null ? pathSchoolId : paramSchoolId;
        if (schoolId == null) {
            throw new IllegalArgumentException("schoolId must be provided either in the path or as a query parameter");
        }
        return schoolId;
    }

    @GetMapping
    @PermissionRequired("staff_hr.payroll.view")
    public ResponseEntity<List<StaffAdvanceDTO>> getAdvances(
            @PathVariable(value = "schoolId", required = false) Long pathSchoolId,
            @RequestParam(value = "schoolId", required = false) Long paramSchoolId,
            @RequestParam(required = false) Long staffId,
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(payrollService.getAdvances(resolveSchoolId(pathSchoolId, paramSchoolId), staffId, status));
    }

    @PostMapping
    @PermissionRequired("staff_hr.payroll.view")
    public ResponseEntity<StaffAdvanceDTO> issueAdvance(
            @PathVariable(value = "schoolId", required = false) Long pathSchoolId,
            @RequestParam(value = "schoolId", required = false) Long paramSchoolId,
            @Valid @RequestBody StaffAdvanceRequest request) {
        return ResponseEntity.ok(payrollService.issueAdvance(resolveSchoolId(pathSchoolId, paramSchoolId), request));
    }

    @PatchMapping("/{id}/status")
    @PermissionRequired("staff_hr.payroll.view")
    public ResponseEntity<StaffAdvanceDTO> updateAdvanceStatus(
            @PathVariable(value = "schoolId", required = false) Long pathSchoolId,
            @RequestParam(value = "schoolId", required = false) Long paramSchoolId,
            @PathVariable Long id,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) BigDecimal recoveredAmount,
            @RequestBody(required = false) Map<String, Object> body) {
        String effectiveStatus = status;
        BigDecimal effectiveRecovered = recoveredAmount;

        if (body != null) {
            if ((effectiveStatus == null || effectiveStatus.isEmpty()) && body.containsKey("status")) {
                effectiveStatus = (String) body.get("status");
            }
            if (effectiveRecovered == null && body.containsKey("recoveredAmount")) {
                effectiveRecovered = new BigDecimal(body.get("recoveredAmount").toString());
            }
        }

        return ResponseEntity.ok(payrollService.updateAdvanceStatus(resolveSchoolId(pathSchoolId, paramSchoolId), id, effectiveStatus, effectiveRecovered));
    }
}
