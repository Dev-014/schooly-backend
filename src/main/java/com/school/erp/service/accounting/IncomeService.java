package com.school.erp.service.accounting;

import com.school.erp.dto.accounting.IncomeHeadRequest;
import com.school.erp.dto.accounting.IncomeHeadResponse;
import com.school.erp.dto.accounting.IncomeRequest;
import com.school.erp.dto.accounting.IncomeResponse;
import com.school.erp.entity.accounting.Income;
import com.school.erp.entity.accounting.IncomeHead;
import com.school.erp.entity.superadmin.School;
import com.school.erp.exception.ResourceNotFoundException;
import com.school.erp.repository.accounting.IncomeHeadRepository;
import com.school.erp.repository.accounting.IncomeRepository;
import com.school.erp.repository.superadmin.SchoolRepository;
import com.school.erp.security.AuthContextService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class IncomeService {

    private final IncomeRepository incomeRepository;
    private final IncomeHeadRepository incomeHeadRepository;
    private final SchoolRepository schoolRepository;
    private final AuthContextService authContextService;

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
    public List<IncomeResponse> getAllIncomes() {
        School school = resolveSchool();
        return incomeRepository.findBySchool(school).stream()
                .map(this::mapToIncomeResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public IncomeResponse createIncome(IncomeRequest request) {
        School school = resolveSchool();
        IncomeHead head = incomeHeadRepository.findByIdAndSchool(request.getIncomeHeadId(), school)
                .orElseThrow(() -> new ResourceNotFoundException("IncomeHead not found"));

        Income income = new Income();
        income.setSchool(school);
        income.setIncomeHead(head);
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
        income.setPaymentMode(request.getPaymentMode());
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

    private IncomeResponse mapToIncomeResponse(Income income) {
        IncomeResponse response = new IncomeResponse();
        response.setId(income.getId());
        response.setIncomeHead(mapToIncomeHeadResponse(income.getIncomeHead()));
        response.setAccountType(income.getAccountType());
        response.setAccountName(income.getAccountName());
        response.setIncomeFrom(income.getIncomeFrom());
        response.setInvoiceNumber(income.getInvoiceNumber());
        response.setAmount(income.getAmount());
        response.setDate(income.getDate());
        response.setPaymentMode(income.getPaymentMode());
        response.setDocumentUrl(income.getDocumentUrl());
        response.setDescription(income.getDescription());
        response.setCreatedAt(income.getCreatedAt());
        response.setUpdatedAt(income.getUpdatedAt());
        return response;
    }
}
