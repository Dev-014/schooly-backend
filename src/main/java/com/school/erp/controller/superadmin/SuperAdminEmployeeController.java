package com.school.erp.controller.superadmin;

import com.school.erp.dto.CreateSuperAdminEmployeeRequest;
import com.school.erp.dto.SuperAdminEmployeeDTO;
import com.school.erp.dto.UpdateSuperAdminEmployeeRequest;
import com.school.erp.service.superadmin.SuperAdminEmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/super-admin/employees")
@RequiredArgsConstructor
public class SuperAdminEmployeeController {

    private final SuperAdminEmployeeService employeeService;

    @GetMapping
    public ResponseEntity<List<SuperAdminEmployeeDTO>> getAllEmployees() {
        return ResponseEntity.ok(employeeService.getAllEmployees());
    }

    @PostMapping
    public ResponseEntity<SuperAdminEmployeeDTO> createEmployee(@Valid @RequestBody CreateSuperAdminEmployeeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(employeeService.createEmployee(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SuperAdminEmployeeDTO> updateEmployee(
            @PathVariable Long id,
            @Valid @RequestBody UpdateSuperAdminEmployeeRequest request) {
        return ResponseEntity.ok(employeeService.updateEmployee(id, request));
    }
}
