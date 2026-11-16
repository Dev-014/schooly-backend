package com.school.erp.service.accounting;

import com.school.erp.dto.accounting.ExpenseHeadRequest;
import com.school.erp.dto.accounting.ExpenseHeadResponse;
import com.school.erp.dto.accounting.ExpenseRequest;
import com.school.erp.dto.accounting.ExpenseResponse;
import com.school.erp.dto.accounting.AccountingSearchFilter;
import com.school.erp.dto.accounting.AccountingSummaryResponse;
import com.school.erp.dto.accounting.AccountingDocumentResponse;
import com.school.erp.entity.academic.AcademicYear;
import com.school.erp.entity.accounting.Expense;
import com.school.erp.entity.accounting.ExpenseHead;
import com.school.erp.entity.file.FileAttachment;
import com.school.erp.entity.file.FileMetadata;
import com.school.erp.entity.superadmin.School;
import com.school.erp.enums.file.FileEntityType;
import com.school.erp.enums.file.FileStatus;
import com.school.erp.enums.accounting.AccountingPaymentMode;
import com.school.erp.exception.BadRequestException;
import com.school.erp.exception.ResourceNotFoundException;
import com.school.erp.repository.academic.AcademicYearRepository;
import com.school.erp.repository.accounting.ExpenseHeadRepository;
import com.school.erp.repository.accounting.ExpenseRepository;
import com.school.erp.repository.file.FileAttachmentRepository;
import com.school.erp.repository.superadmin.SchoolRepository;
import com.school.erp.security.AuthContextService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.time.LocalDate;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final ExpenseHeadRepository expenseHeadRepository;
    private final SchoolRepository schoolRepository;
    private final AuthContextService authContextService;
    private final AcademicYearRepository academicYearRepository;
    private final FileAttachmentRepository fileAttachmentRepository;

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
    public List<ExpenseResponse> getAllExpenses(AccountingSearchFilter filter) {
        School school = resolveSchool();
        normalizeAndValidateFilter(filter, school);
        List<Expense> expenses = expenseRepository.findAllFiltered(
                school.getId(), filter.getAcademicYearId(), filter.getHeadId(),
                filter.getAccountType(), filter.getStartDate(), filter.getEndDate(),
                filter.getMonth(), filter.getYear(), filter.getPaymentMode());
        Map<Long, List<AccountingDocumentResponse>> documentsByExpense = loadDocuments(
                school.getId(), FileEntityType.EXPENSE, expenses.stream().map(Expense::getId).toList());
        return expenses.stream()
                .map(expense -> mapToExpenseResponse(expense,
                        documentsByExpense.getOrDefault(expense.getId(), List.of())))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AccountingSummaryResponse getSummary(AccountingSearchFilter filter) {
        School school = resolveSchool();
        normalizeAndValidateFilter(filter, school);
        return AccountingSummaryMapper.fromGroupedAmounts(expenseRepository.sumFilteredByPaymentMode(
                school.getId(), filter.getAcademicYearId(), filter.getHeadId(),
                filter.getAccountType(), filter.getStartDate(), filter.getEndDate(),
                filter.getMonth(), filter.getYear(), filter.getPaymentMode()));
    }

    @Transactional
    public ExpenseResponse createExpense(ExpenseRequest request) {
        School school = resolveSchool();
        ExpenseHead head = expenseHeadRepository.findByIdAndSchool(request.getExpenseHeadId(), school)
                .orElseThrow(() -> new ResourceNotFoundException("ExpenseHead not found"));

        Expense expense = new Expense();
        expense.setSchool(school);
        expense.setExpenseHead(head);
        if (Boolean.TRUE.equals(request.getClearAcademicYear())) {
            expense.setAcademicYear(null);
        } else if (request.getAcademicYearId() != null) {
            expense.setAcademicYear(resolveAcademicYear(request.getAcademicYearId(), school));
        }
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
        if (Boolean.TRUE.equals(request.getClearAcademicYear())) {
            expense.setAcademicYear(null);
        } else if (request.getAcademicYearId() != null) {
            expense.setAcademicYear(resolveAcademicYear(request.getAcademicYearId(), school));
        }
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
        expense.setPaymentMode(AccountingPaymentMode.canonicalize(request.getPaymentMode()));
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

    private AcademicYear resolveAcademicYear(Long academicYearId, School school) {
        return academicYearRepository.findByIdAndSchoolId(academicYearId, school.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Academic session not found"));
    }

    private void normalizeAndValidateFilter(AccountingSearchFilter filter, School school) {
        if (filter.getMonth() != null && filter.getYear() == null) {
            throw new BadRequestException("A year is required when filtering by month");
        }
        LocalDate startDate = filter.getStartDate();
        LocalDate endDate = filter.getEndDate();
        if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
            throw new BadRequestException("End date must be on or after start date");
        }
        if (filter.getAcademicYearId() != null) {
            resolveAcademicYear(filter.getAcademicYearId(), school);
        }
        if (filter.getPaymentMode() != null) {
            filter.setPaymentMode(AccountingPaymentMode.canonicalize(filter.getPaymentMode()));
        }
        if (filter.getAccountType() != null && filter.getAccountType().isBlank()) {
            filter.setAccountType(null);
        }
    }

    private Map<Long, List<AccountingDocumentResponse>> loadDocuments(
            Long schoolId, FileEntityType entityType, List<Long> entityIds) {
        if (entityIds.isEmpty()) {
            return Map.of();
        }
        Map<Long, List<AccountingDocumentResponse>> documentsByEntity = new HashMap<>();
        for (FileAttachment attachment : fileAttachmentRepository
                .findBySchoolIdAndEntityTypeAndEntityIdInOrderByDisplayOrderAscCreatedAtDesc(
                        schoolId, entityType, entityIds)) {
            FileMetadata file = attachment.getFile();
            if (file.getDeletedAt() != null || file.getStatus() == FileStatus.DELETED || file.getStatus() == FileStatus.REJECTED || file.getStatus() == FileStatus.FAILED) {
                continue;
            }
            documentsByEntity.computeIfAbsent(attachment.getEntityId(), ignored -> new ArrayList<>())
                    .add(new AccountingDocumentResponse(
                            file.getId(),
                            file.getOriginalFileName(),
                            file.getMimeType(),
                            file.getFileSizeBytes()
                    ));
        }
        return documentsByEntity;
    }

    private ExpenseResponse mapToExpenseResponse(Expense expense) {
        return mapToExpenseResponse(expense, loadDocuments(
                expense.getSchool().getId(), FileEntityType.EXPENSE, List.of(expense.getId()))
                .getOrDefault(expense.getId(), List.of()));
    }

    private ExpenseResponse mapToExpenseResponse(Expense expense, List<AccountingDocumentResponse> documents) {
        ExpenseResponse response = new ExpenseResponse();
        response.setId(expense.getId());
        if (expense.getAcademicYear() != null) {
            response.setAcademicYearId(expense.getAcademicYear().getId());
            response.setAcademicYearName(expense.getAcademicYear().getDisplayName() != null
                    ? expense.getAcademicYear().getDisplayName()
                    : expense.getAcademicYear().getName());
        }
        response.setExpenseHead(mapToExpenseHeadResponse(expense.getExpenseHead()));
        response.setAccountType(expense.getAccountType());
        response.setAccountName(expense.getAccountName());
        response.setVendorName(expense.getVendorName());
        response.setInvoiceNumber(expense.getInvoiceNumber());
        response.setAmount(expense.getAmount());
        response.setDate(expense.getDate());
        response.setPaymentMode(expense.getPaymentMode());
        response.setDocumentUrl(expense.getDocumentUrl());
        response.setDocuments(documents);
        response.setDescription(expense.getDescription());
        response.setCreatedAt(expense.getCreatedAt());
        response.setUpdatedAt(expense.getUpdatedAt());
        return response;
    }
}
