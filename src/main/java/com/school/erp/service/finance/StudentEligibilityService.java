package com.school.erp.service.finance;

import com.school.erp.entity.FeeDue;
import com.school.erp.repository.FeeDueRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class StudentEligibilityService {

    private final FeeDueRepository feeDueRepository;

    public StudentEligibilityService(FeeDueRepository feeDueRepository) {
        this.feeDueRepository = feeDueRepository;
    }

    public EligibilityStatus getEligibility(Long studentId, Long schoolId) {
        List<FeeDue> dues = feeDueRepository.findByStudentIdAndSchoolIdOrderByDueDateAsc(studentId, schoolId);
        
        BigDecimal totalDueAmount = BigDecimal.ZERO;
        BigDecimal totalPaidAmount = BigDecimal.ZERO;
        
        for (FeeDue due : dues) {
            totalDueAmount = totalDueAmount.add(due.getAmount());
            totalPaidAmount = totalPaidAmount.add(due.getPaidAmount() != null ? due.getPaidAmount() : BigDecimal.ZERO)
                    .add(due.getDiscountAmount() != null ? due.getDiscountAmount() : BigDecimal.ZERO);
        }
        
        BigDecimal outstanding = totalDueAmount.subtract(totalPaidAmount);
        
        // Simple logic for MVP: 
        // Eligible if outstanding is less than or equal to 0 (meaning all dues are paid)
        // This can be enhanced later to check specific terms or threshold amounts
        
        boolean isEligible = outstanding.compareTo(BigDecimal.ZERO) <= 0;
        
        return new EligibilityStatus(studentId, isEligible, outstanding, isEligible ? "Cleared" : "Pending Dues");
    }

    public record EligibilityStatus(Long studentId, boolean isEligible, BigDecimal outstandingBalance, String reason) {}
}
