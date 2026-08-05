package com.school.erp.controller.superadmin;

import com.school.erp.dto.EmployeeLifecycleDTO;
import com.school.erp.service.superadmin.EmployeeLifecycleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/super-admin/employee-lifecycle")
@RequiredArgsConstructor
public class EmployeeLifecycleController {

    private final EmployeeLifecycleService lifecycleService;

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<EmployeeLifecycleDTO>> getLifecycleEventsByEmployeeId(@PathVariable Long employeeId) {
        return ResponseEntity.ok(lifecycleService.getLifecycleEventsByEmployeeId(employeeId));
    }
}
