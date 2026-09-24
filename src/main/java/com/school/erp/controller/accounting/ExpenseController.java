package com.school.erp.controller.accounting;

import com.school.erp.dto.accounting.ExpenseHeadRequest;
import com.school.erp.dto.accounting.ExpenseHeadResponse;
import com.school.erp.dto.accounting.ExpenseRequest;
import com.school.erp.dto.accounting.ExpenseResponse;
import com.school.erp.service.accounting.ExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.school.erp.security.PermissionRequired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    // --- Expense Heads ---

    @GetMapping("/heads")
    @PermissionRequired("expense.expense_head.view")
    public ResponseEntity<List<ExpenseHeadResponse>> getAllExpenseHeads() {
        return ResponseEntity.ok(expenseService.getAllExpenseHeads());
    }

    @PostMapping("/heads")
    @PermissionRequired("expense.transaction.edit")
    public ResponseEntity<ExpenseHeadResponse> createExpenseHead(@Valid @RequestBody ExpenseHeadRequest request) {
        return new ResponseEntity<>(expenseService.createExpenseHead(request), HttpStatus.CREATED);
    }

    @PutMapping("/heads/{id}")
    @PermissionRequired("expense.transaction.edit")
    public ResponseEntity<ExpenseHeadResponse> updateExpenseHead(
            @PathVariable Long id, @Valid @RequestBody ExpenseHeadRequest request) {
        return ResponseEntity.ok(expenseService.updateExpenseHead(id, request));
    }

    @DeleteMapping("/heads/{id}")
    @PermissionRequired("expense.transaction.edit")
    public ResponseEntity<Void> deleteExpenseHead(@PathVariable Long id) {
        expenseService.deleteExpenseHead(id);
        return ResponseEntity.noContent().build();
    }

    // --- Expenses ---

    @GetMapping
    @PermissionRequired("expense.transaction.view")
    public ResponseEntity<List<ExpenseResponse>> getAllExpenses() {
        return ResponseEntity.ok(expenseService.getAllExpenses());
    }

    @PostMapping
    @PermissionRequired("expense.transaction.edit")
    public ResponseEntity<ExpenseResponse> createExpense(@Valid @RequestBody ExpenseRequest request) {
        return new ResponseEntity<>(expenseService.createExpense(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PermissionRequired("expense.transaction.edit")
    public ResponseEntity<ExpenseResponse> updateExpense(
            @PathVariable Long id, @Valid @RequestBody ExpenseRequest request) {
        return ResponseEntity.ok(expenseService.updateExpense(id, request));
    }

    @DeleteMapping("/{id}")
    @PermissionRequired("expense.transaction.edit")
    public ResponseEntity<Void> deleteExpense(@PathVariable Long id) {
        expenseService.deleteExpense(id);
        return ResponseEntity.noContent().build();
    }
}
