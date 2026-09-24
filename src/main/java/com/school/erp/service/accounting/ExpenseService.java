package com.school.erp.service.accounting;

import com.school.erp.dto.accounting.ExpenseHeadRequest;
import com.school.erp.dto.accounting.ExpenseHeadResponse;
import com.school.erp.dto.accounting.ExpenseRequest;
import com.school.erp.dto.accounting.ExpenseResponse;
import com.school.erp.entity.accounting.Expense;
import com.school.erp.entity.accounting.ExpenseHead;
import com.school.erp.entity.superadmin.School;
import com.school.erp.exception.ResourceNotFoundException;
import com.school.erp.repository.accounting.ExpenseHeadRepository;
import com.school.erp.repository.accounting.ExpenseRepository;
import com.school.erp.repository.superadmin.SchoolRepository;
import com.school.erp.security.AuthContextService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final ExpenseHeadRepository expenseHeadRepository;
    private final SchoolRepository schoolRepository;
    private final AuthContextService authContextService;

    private School resolveSchool() {
        Long schoolId = authContextService.resolveSchoolId(null);
        return schoolRepository.findById(schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("School not found"));
    }

    @Transactional(readOnly = true)
    public List<ExpenseHeadResponse> getAllExpenseHeads() {
        School school = resolveSchool();
        return expenseHeadRepository.findBySchool(school).stream()
                .map(this::mapToExpenseHeadResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ExpenseHeadResponse createExpenseHead(ExpenseHeadRequest request) {
        School school = resolveSchool();
        ExpenseHead head = new ExpenseHead();
        head.setSchool(school);
        head.setName(request.getName());
        head.setDescription(request.getDescription());
        return mapToExpenseHeadResponse(expenseHeadRepository.save(head));
    }

    @Transactional
    public ExpenseHeadResponse updateExpenseHead(Long id, ExpenseHeadRequest request) {
        School school = resolveSchool();
        ExpenseHead head = expenseHeadRepository.findByIdAndSchool(id, school)
                .orElseThrow(() -> new ResourceNotFoundException("ExpenseHead not found"));
        head.setName(request.getName());
        head.setDescription(request.getDescription());
        return mapToExpenseHeadResponse(expenseHeadRepository.save(head));
    }

    @Transactional
    public void deleteExpenseHead(Long id) {
        School school = resolveSchool();
        ExpenseHead head = expenseHeadRepository.findByIdAndSchool(id, school)
                .orElseThrow(() -> new ResourceNotFoundException("ExpenseHead not found"));
        expenseHeadRepository.delete(head);
    }

    @Transactional(readOnly = true)
    public List<ExpenseResponse> getAllExpenses() {
        School school = resolveSchool();
        return expenseRepository.findBySchool(school).stream()
                .map(this::mapToExpenseResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ExpenseResponse createExpense(ExpenseRequest request) {
        School school = resolveSchool();
        ExpenseHead head = expenseHeadRepository.findByIdAndSchool(request.getExpenseHeadId(), school)
                .orElseThrow(() -> new ResourceNotFoundException("ExpenseHead not found"));

        Expense expense = new Expense();
        expense.setSchool(school);
        expense.setExpenseHead(head);
        updateExpenseFromRequest(expense, request);
        return mapToExpenseResponse(expenseRepository.save(expense));
    }

    @Transactional
    public ExpenseResponse updateExpense(Long id, ExpenseRequest request) {
        School school = resolveSchool();
        Expense expense = expenseRepository.findByIdAndSchool(id, school)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found"));
        
        ExpenseHead head = expenseHeadRepository.findByIdAndSchool(request.getExpenseHeadId(), school)
                .orElseThrow(() -> new ResourceNotFoundException("ExpenseHead not found"));
        
        expense.setExpenseHead(head);
        updateExpenseFromRequest(expense, request);
        return mapToExpenseResponse(expenseRepository.save(expense));
    }

    @Transactional
    public void deleteExpense(Long id) {
        School school = resolveSchool();
        Expense expense = expenseRepository.findByIdAndSchool(id, school)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found"));
        expenseRepository.delete(expense);
    }

    private void updateExpenseFromRequest(Expense expense, ExpenseRequest request) {
        expense.setAccountType(request.getAccountType());
        expense.setAccountName(request.getAccountName());
        expense.setVendorName(request.getVendorName());
        expense.setInvoiceNumber(request.getInvoiceNumber());
        expense.setAmount(request.getAmount());
        expense.setDate(request.getDate());
        expense.setPaymentMode(request.getPaymentMode());
        expense.setDocumentUrl(request.getDocumentUrl());
        expense.setDescription(request.getDescription());
    }

    private ExpenseHeadResponse mapToExpenseHeadResponse(ExpenseHead head) {
        ExpenseHeadResponse response = new ExpenseHeadResponse();
        response.setId(head.getId());
        response.setName(head.getName());
        response.setDescription(head.getDescription());
        response.setCreatedAt(head.getCreatedAt());
        response.setUpdatedAt(head.getUpdatedAt());
        return response;
    }

    private ExpenseResponse mapToExpenseResponse(Expense expense) {
        ExpenseResponse response = new ExpenseResponse();
        response.setId(expense.getId());
        response.setExpenseHead(mapToExpenseHeadResponse(expense.getExpenseHead()));
        response.setAccountType(expense.getAccountType());
        response.setAccountName(expense.getAccountName());
        response.setVendorName(expense.getVendorName());
        response.setInvoiceNumber(expense.getInvoiceNumber());
        response.setAmount(expense.getAmount());
        response.setDate(expense.getDate());
        response.setPaymentMode(expense.getPaymentMode());
        response.setDocumentUrl(expense.getDocumentUrl());
        response.setDescription(expense.getDescription());
        response.setCreatedAt(expense.getCreatedAt());
        response.setUpdatedAt(expense.getUpdatedAt());
        return response;
    }
}
