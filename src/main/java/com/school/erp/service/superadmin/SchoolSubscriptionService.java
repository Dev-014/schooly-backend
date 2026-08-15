package com.school.erp.service.superadmin;

import com.school.erp.dto.superadmin.SchoolSubscriptionDTO;
import com.school.erp.dto.superadmin.SchoolSubscriptionInstallmentDTO;
import com.school.erp.entity.SchoolSubscription;
import com.school.erp.entity.SchoolSubscriptionInstallment;
import com.school.erp.repository.SchoolSubscriptionInstallmentRepository;
import com.school.erp.repository.SchoolSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SchoolSubscriptionService {

    private final SchoolSubscriptionRepository subscriptionRepository;
    private final SchoolSubscriptionInstallmentRepository installmentRepository;

    @Transactional(readOnly = true)
    public SchoolSubscriptionDTO getActiveSubscriptionForSchool(Long schoolId) {
        SchoolSubscription subscription = subscriptionRepository.findFirstBySchoolIdAndStatusOrderByIdDesc(schoolId, "ACTIVE")
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, 
                        "Active subscription not found for school " + schoolId));

        return mapToDTO(subscription);
    }

    private SchoolSubscriptionDTO mapToDTO(SchoolSubscription subscription) {
        SchoolSubscriptionDTO dto = new SchoolSubscriptionDTO();
        dto.setId(subscription.getId());
        dto.setSchoolId(subscription.getSchool().getId());
        dto.setPlanName(subscription.getPlan().getName());
        dto.setBillingPeriod(subscription.getBillingPeriod());
        dto.setTotalStudents(subscription.getTotalStudents());
        dto.setTotalAmount(subscription.getTotalAmount());
        dto.setAmountPaid(subscription.getAmountPaid());
        dto.setRemainingAmount(subscription.getRemainingAmount());
        dto.setStatus(subscription.getStatus());
        dto.setStartDate(subscription.getStartDate());
        dto.setEndDate(subscription.getEndDate());

        List<SchoolSubscriptionInstallmentDTO> installmentDTOs = subscription.getInstallments().stream()
                .map(this::mapInstallmentToDTO)
                .collect(Collectors.toList());
        dto.setInstallments(installmentDTOs);

        return dto;
    }

    private SchoolSubscriptionInstallmentDTO mapInstallmentToDTO(SchoolSubscriptionInstallment installment) {
        SchoolSubscriptionInstallmentDTO dto = new SchoolSubscriptionInstallmentDTO();
        dto.setId(installment.getId());
        dto.setInstallmentNumber(installment.getInstallmentNumber());
        dto.setAmount(installment.getAmount());
        dto.setDueDate(installment.getDueDate());
        dto.setStatus(installment.getStatus());
        dto.setPaidDate(installment.getPaidDate());
        return dto;
    }
}
