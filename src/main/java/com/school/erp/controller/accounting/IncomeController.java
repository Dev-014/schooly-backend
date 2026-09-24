package com.school.erp.controller.accounting;

import com.school.erp.dto.accounting.IncomeHeadRequest;
import com.school.erp.dto.accounting.IncomeHeadResponse;
import com.school.erp.dto.accounting.IncomeRequest;
import com.school.erp.dto.accounting.IncomeResponse;
import com.school.erp.service.accounting.IncomeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.school.erp.security.PermissionRequired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/income")
@RequiredArgsConstructor
public class IncomeController {

    private final IncomeService incomeService;

    // --- Income Heads ---

    @GetMapping("/heads")
    @PermissionRequired("income.income_head.view")
    public ResponseEntity<List<IncomeHeadResponse>> getAllIncomeHeads() {
        return ResponseEntity.ok(incomeService.getAllIncomeHeads());
    }

    @PostMapping("/heads")
    @PermissionRequired("income.transaction.edit")
    public ResponseEntity<IncomeHeadResponse> createIncomeHead(@Valid @RequestBody IncomeHeadRequest request) {
        return new ResponseEntity<>(incomeService.createIncomeHead(request), HttpStatus.CREATED);
    }

    @PutMapping("/heads/{id}")
    @PermissionRequired("income.transaction.edit")
    public ResponseEntity<IncomeHeadResponse> updateIncomeHead(
            @PathVariable Long id, @Valid @RequestBody IncomeHeadRequest request) {
        return ResponseEntity.ok(incomeService.updateIncomeHead(id, request));
    }

    @DeleteMapping("/heads/{id}")
    @PermissionRequired("income.transaction.edit")
    public ResponseEntity<Void> deleteIncomeHead(@PathVariable Long id) {
        incomeService.deleteIncomeHead(id);
        return ResponseEntity.noContent().build();
    }

    // --- Incomes ---

    @GetMapping
    @PermissionRequired("income.transaction.view")
    public ResponseEntity<List<IncomeResponse>> getAllIncomes() {
        return ResponseEntity.ok(incomeService.getAllIncomes());
    }

    @PostMapping
    @PermissionRequired("income.transaction.edit")
    public ResponseEntity<IncomeResponse> createIncome(@Valid @RequestBody IncomeRequest request) {
        return new ResponseEntity<>(incomeService.createIncome(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PermissionRequired("income.transaction.edit")
    public ResponseEntity<IncomeResponse> updateIncome(
            @PathVariable Long id, @Valid @RequestBody IncomeRequest request) {
        return ResponseEntity.ok(incomeService.updateIncome(id, request));
    }

    @DeleteMapping("/{id}")
    @PermissionRequired("income.transaction.edit")
    public ResponseEntity<Void> deleteIncome(@PathVariable Long id) {
        incomeService.deleteIncome(id);
        return ResponseEntity.noContent().build();
    }
}
