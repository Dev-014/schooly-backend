package com.school.erp.service.finance;

import com.school.erp.dto.finance.FeeInstallmentItemRequest;
import com.school.erp.dto.finance.FeeInstallmentItemResponse;
import com.school.erp.dto.finance.FeeInstallmentRequest;
import com.school.erp.dto.finance.FeeInstallmentResponse;
import com.school.erp.entity.*;
import com.school.erp.exception.ResourceNotFoundException;
import com.school.erp.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FeeInstallmentService {

    private final FeeInstallmentRepository feeInstallmentRepository;
    private final FeeInstallmentItemRepository feeInstallmentItemRepository;
    private final SchoolRepository schoolRepository;
    private final AcademicYearRepository academicYearRepository;
    private final FeeCategoryRepository feeCategoryRepository;

    @Transactional
    public FeeInstallmentResponse createInstallment(Long schoolId, FeeInstallmentRequest request) {
        School school = schoolRepository.findById(schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("School not found"));

        AcademicYear academicYear = academicYearRepository.findById(request.getAcademicYearId())
                .orElseThrow(() -> new ResourceNotFoundException("Academic year not found"));

        FeeInstallment installment = new FeeInstallment();
        installment.setSchool(school);
        installment.setAcademicYear(academicYear);
        installment.setName(request.getName());
        installment.setStartDate(request.getStartDate());
        installment.setDueDate(request.getDueDate());

        installment = feeInstallmentRepository.save(installment);

        // Add items
        if (request.getItems() != null) {
            for (FeeInstallmentItemRequest itemReq : request.getItems()) {
                FeeCategory category = feeCategoryRepository.findById(itemReq.getFeeCategoryId())
                        .orElseThrow(() -> new ResourceNotFoundException("Fee category not found"));

                FeeInstallmentItem item = new FeeInstallmentItem();
                item.setFeeInstallment(installment);
                item.setFeeCategory(category);
                item.setAmount(itemReq.getAmount());
                feeInstallmentItemRepository.save(item);
            }
        }

        return mapToResponse(installment, feeInstallmentItemRepository.findByFeeInstallmentId(installment.getId()));
    }

    @Transactional(readOnly = true)
    public List<FeeInstallmentResponse> getInstallments(Long schoolId, Long academicYearId) {
        List<FeeInstallment> installments;
        if (academicYearId != null) {
            installments = feeInstallmentRepository.findBySchoolIdAndAcademicYearId(schoolId, academicYearId);
        } else {
            installments = feeInstallmentRepository.findBySchoolId(schoolId);
        }

        return installments.stream()
                .map(i -> mapToResponse(i, feeInstallmentItemRepository.findByFeeInstallmentId(i.getId())))
                .collect(Collectors.toList());
    }

    @Transactional
    public FeeInstallmentResponse updateInstallment(Long id, Long schoolId, FeeInstallmentRequest request) {
        FeeInstallment installment = feeInstallmentRepository.findById(id)
                .filter(i -> i.getSchool().getId().equals(schoolId))
                .orElseThrow(() -> new ResourceNotFoundException("Fee Installment not found"));

        AcademicYear academicYear = academicYearRepository.findById(request.getAcademicYearId())
                .orElseThrow(() -> new ResourceNotFoundException("Academic year not found"));

        installment.setAcademicYear(academicYear);
        installment.setName(request.getName());
        installment.setStartDate(request.getStartDate());
        installment.setDueDate(request.getDueDate());

        installment = feeInstallmentRepository.save(installment);

        // Update items (replace all for simplicity)
        List<FeeInstallmentItem> existingItems = feeInstallmentItemRepository.findByFeeInstallmentId(installment.getId());
        feeInstallmentItemRepository.deleteAll(existingItems);
        feeInstallmentItemRepository.flush();

        if (request.getItems() != null) {
            for (FeeInstallmentItemRequest itemReq : request.getItems()) {
                FeeCategory category = feeCategoryRepository.findById(itemReq.getFeeCategoryId())
                        .orElseThrow(() -> new ResourceNotFoundException("Fee category not found"));

                FeeInstallmentItem item = new FeeInstallmentItem();
                item.setFeeInstallment(installment);
                item.setFeeCategory(category);
                item.setAmount(itemReq.getAmount());
                feeInstallmentItemRepository.save(item);
            }
        }

        return mapToResponse(installment, feeInstallmentItemRepository.findByFeeInstallmentId(installment.getId()));
    }

    @Transactional
    public void deleteInstallment(Long id, Long schoolId) {
        FeeInstallment installment = feeInstallmentRepository.findById(id)
                .filter(i -> i.getSchool().getId().equals(schoolId))
                .orElseThrow(() -> new ResourceNotFoundException("Fee Installment not found"));

        feeInstallmentRepository.delete(installment);
    }

    private FeeInstallmentResponse mapToResponse(FeeInstallment installment, List<FeeInstallmentItem> items) {
        FeeInstallmentResponse response = new FeeInstallmentResponse();
        response.setId(installment.getId());
        response.setAcademicYearId(installment.getAcademicYear().getId());
        response.setName(installment.getName());
        response.setStartDate(installment.getStartDate());
        response.setDueDate(installment.getDueDate());
        
        List<FeeInstallmentItemResponse> itemResponses = items.stream().map(item -> {
            FeeInstallmentItemResponse itemResponse = new FeeInstallmentItemResponse();
            itemResponse.setId(item.getId());
            itemResponse.setFeeCategoryId(item.getFeeCategory().getId());
            itemResponse.setCategoryName(item.getFeeCategory().getName());
            itemResponse.setAmount(item.getAmount());
            return itemResponse;
        }).collect(Collectors.toList());
        
        response.setItems(itemResponses);
        return response;
    }
}
