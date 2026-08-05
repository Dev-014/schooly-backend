package com.school.erp.controller.superadmin;

import com.school.erp.dto.EmployeeAssetDTO;
import com.school.erp.service.superadmin.EmployeeAssetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/super-admin/employee-assets")
@RequiredArgsConstructor
public class EmployeeAssetController {

    private final EmployeeAssetService assetService;

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<EmployeeAssetDTO>> getAssetsByEmployeeId(@PathVariable Long employeeId) {
        return ResponseEntity.ok(assetService.getAssetsByEmployeeId(employeeId));
    }
}
