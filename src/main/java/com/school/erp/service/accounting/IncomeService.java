package com.school.erp.service.accounting;

import com.school.erp.dto.accounting.IncomeHeadRequest;
import com.school.erp.dto.accounting.IncomeHeadResponse;
import com.school.erp.dto.accounting.IncomeRequest;
import com.school.erp.dto.accounting.IncomeResponse;
import com.school.erp.dto.accounting.AccountingSearchFilter;
import com.school.erp.dto.accounting.AccountingSummaryResponse;
import com.school.erp.dto.accounting.AccountingDocumentResponse;
import com.school.erp.entity.academic.AcademicYear;
import com.school.erp.entity.accounting.Income;
import com.school.erp.entity.accounting.IncomeHead;
import com.school.erp.entity.file.FileAttachment;
import com.school.erp.entity.file.FileMetadata;
import com.school.erp.entity.superadmin.School;
import com.school.erp.enums.file.FileEntityType;
import com.school.erp.enums.file.FileStatus;
import com.school.erp.enums.accounting.AccountingPaymentMode;
import com.school.erp.exception.BadRequestException;
import com.school.erp.exception.ResourceNotFoundException;
import com.school.erp.repository.academic.AcademicYearRepository;
import com.school.erp.repository.accounting.IncomeHeadRepository;
import com.school.erp.repository.accounting.IncomeRepository;
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
public class IncomeService {

    private final IncomeRepository incomeRepository;
    private final IncomeHeadRepository incomeHeadRepository;
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
    public List<IncomeHeadResponse> getAllIncomeHeads() {
        School school = resolveSchool();
        return incomeHeadRepository.findBySchool(school).stream()
                .map(this::mapToIncomeHeadResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public IncomeHeadResponse createIncomeHead(IncomeHeadRequest request) {
        School school = resolveSchool();
        IncomeHead head = new IncomeHead();
        head.setSchool(school);
        head.setName(request.getName());
        head.setDescription(request.getDescription());
        return mapToIncomeHeadResponse(incomeHeadRepository.save(head));
    }

    @Transactional
    public IncomeHeadResponse updateIncomeHead(Long id, IncomeHeadRequest request) {
        School school = resolveSchool();
        IncomeHead head = incomeHeadRepository.findByIdAndSchool(id, school)
                .orElseThrow(() -> new ResourceNotFoundException("IncomeHead not found"));
        head.setName(request.getName());
        head.setDescription(request.getDescription());
        return mapToIncomeHeadResponse(incomeHeadRepository.save(head));
    }

    @Transactional
    public void deleteIncomeHead(Long id) {
        School school = resolveSchool();
        IncomeHead head = incomeHeadRepository.findByIdAndSchool(id, school)
                .orElseThrow(() -> new ResourceNotFoundException("IncomeHead not found"));
        incomeHeadRepository.delete(head);
    }

    @Transactional(readOnly = true)
    public List<IncomeResponse> getAllIncomes(AccountingSearchFilter filter) {
        School school = resolveSchool();
        normalizeAndValidateFilter(filter, school);
        List<Income> incomes = incomeRepository.findAllFiltered(
                school.getId(), filter.getAcademicYearId(), filter.getHeadId(),
                filter.getAccountType(), filter.getStartDate(), filter.getEndDate(),
                filter.getMonth(), filter.getYear(), filter.getPaymentMode());
        Map<Long, List<AccountingDocumentResponse>> documentsByIncome = loadDocuments(
                school.getId(), FileEntityType.INCOME, incomes.stream().map(Income::getId).toList());
        return incomes.stream()
                .map(income -> mapToIncomeResponse(income,
                        documentsByIncome.getOrDefault(income.getId(), List.of())))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AccountingSummaryResponse getSummary(AccountingSearchFilter filter) {
        School school = resolveSchool();
        normalizeAndValidateFilter(filter, school);
        return AccountingSummaryMapper.fromGroupedAmounts(incomeRepository.sumFilteredByPaymentMode(
                school.getId(), filter.getAcademicYearId(), filter.getHeadId(),
                filter.getAccountType(), filter.getStartDate(), filter.getEndDate(),
                filter.getMonth(), filter.getYear(), filter.getPaymentMode()));
    }

    @Transactional
    public IncomeResponse createIncome(IncomeRequest request) {
        School school = resolveSchool();
        IncomeHead head = incomeHeadRepository.findByIdAndSchool(request.getIncomeHeadId(), school)
                .orElseThrow(() -> new ResourceNotFoundException("IncomeHead not found"));

        Income income = new Income();
        income.setSchool(school);
        income.setIncomeHead(head);
        if (Boolean.TRUE.equals(request.getClearAcademicYear())) {
            income.setAcademicYear(null);
        } else if (request.getAcademicYearId() != null) {
            income.setAcademicYear(resolveAcademicYear(request.getAcademicYearId(), school));
        }
        updateIncomeFromRequest(income, request);
        return mapToIncomeResponse(incomeRepository.save(income));
    }

    @Transactional
    public IncomeResponse updateIncome(Long id, IncomeRequest request) {
        School school = resolveSchool();
        Income income = incomeRepository.findByIdAndSchool(id, school)
                .orElseThrow(() -> new ResourceNotFoundException("Income not found"));
        
        IncomeHead head = incomeHeadRepository.findByIdAndSchool(request.getIncomeHeadId(), school)
                .orElseThrow(() -> new ResourceNotFoundException("IncomeHead not found"));
        
        income.setIncomeHead(head);
        if (Boolean.TRUE.equals(request.getClearAcademicYear())) {
            income.setAcademicYear(null);
        } else if (request.getAcademicYearId() != null) {
            income.setAcademicYear(resolveAcademicYear(request.getAcademicYearId(), school));
        }
        updateIncomeFromRequest(income, request);
        return mapToIncomeResponse(incomeRepository.save(income));
    }

    @Transactional
    public void deleteIncome(Long id) {
        School school = resolveSchool();
        Income income = incomeRepository.findByIdAndSchool(id, school)
                .orElseThrow(() -> new ResourceNotFoundException("Income not found"));
        incomeRepository.delete(income);
    }

    private void updateIncomeFromRequest(Income income, IncomeRequest request) {
        income.setAccountType(request.getAccountType());
        income.setAccountName(request.getAccountName());
        income.setIncomeFrom(request.getIncomeFrom());
        income.setInvoiceNumber(request.getInvoiceNumber());
        income.setAmount(request.getAmount());
        income.setDate(request.getDate());
        income.setPaymentMode(AccountingPaymentMode.canonicalize(request.getPaymentMode()));
        income.setDocumentUrl(request.getDocumentUrl());
        income.setDescription(request.getDescription());
    }

    private IncomeHeadResponse mapToIncomeHeadResponse(IncomeHead head) {
        IncomeHeadResponse response = new IncomeHeadResponse();
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

    private IncomeResponse mapToIncomeResponse(Income income) {
        return mapToIncomeResponse(income, loadDocuments(
                income.getSchool().getId(), FileEntityType.INCOME, List.of(income.getId()))
                .getOrDefault(income.getId(), List.of()));
    }

    private IncomeResponse mapToIncomeResponse(Income income, List<AccountingDocumentResponse> documents) {
        IncomeResponse response = new IncomeResponse();
        response.setId(income.getId());
        if (income.getAcademicYear() != null) {
            response.setAcademicYearId(income.getAcademicYear().getId());
            response.setAcademicYearName(income.getAcademicYear().getDisplayName() != null
                    ? income.getAcademicYear().getDisplayName()
                    : income.getAcademicYear().getName());
        }
        response.setIncomeHead(mapToIncomeHeadResponse(income.getIncomeHead()));
        response.setAccountType(income.getAccountType());
        response.setAccountName(income.getAccountName());
        response.setIncomeFrom(income.getIncomeFrom());
        response.setInvoiceNumber(income.getInvoiceNumber());
        response.setAmount(income.getAmount());
        response.setDate(income.getDate());
        response.setPaymentMode(income.getPaymentMode());
        response.setDocumentUrl(income.getDocumentUrl());
        response.setDocuments(documents);
        response.setDescription(income.getDescription());
        response.setCreatedAt(income.getCreatedAt());
        response.setUpdatedAt(income.getUpdatedAt());
        return response;
    }
}
