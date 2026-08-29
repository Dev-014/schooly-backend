package com.school.erp.service.impl;

import com.school.erp.dto.payment.FeePaymentRequest;
import com.school.erp.dto.payment.PaymentResponse;
import com.school.erp.entity.*;
import com.school.erp.repository.*;
import com.school.erp.service.FeePaymentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class FeePaymentServiceImpl implements FeePaymentService {

    private final PaymentRepository paymentRepository;
    private final FeeDueRepository feeDueRepository;
    private final FeePaymentAllocationRepository feePaymentAllocationRepository;
    private final StudentRepository studentRepository;
    private final SchoolRepository schoolRepository;

    public FeePaymentServiceImpl(PaymentRepository paymentRepository, FeeDueRepository feeDueRepository, FeePaymentAllocationRepository feePaymentAllocationRepository, StudentRepository studentRepository, SchoolRepository schoolRepository) {
        this.paymentRepository = paymentRepository;
        this.feeDueRepository = feeDueRepository;
        this.feePaymentAllocationRepository = feePaymentAllocationRepository;
        this.studentRepository = studentRepository;
        this.schoolRepository = schoolRepository;
    }

    @Override
    public PaymentResponse processPayment(FeePaymentRequest request) {
        School school = schoolRepository.findById(request.getSchoolId()).orElseThrow(() -> new RuntimeException("School not found"));
        Student student = studentRepository.findById(request.getStudentId()).orElseThrow(() -> new RuntimeException("Student not found"));

        Payment payment = new Payment();
        payment.setSchool(school);
        payment.setStudent(student);
        payment.setAmount(request.getTotalAmount());
        payment.setUnallocatedAmount(request.getTotalAmount());
        payment.setPaymentMode(request.getPaymentMethod());
        payment.setPaymentDate(request.getPaymentDate());
        payment.setStatus("SUCCESS");
        payment.setReceiptNumber("REC-" + UUID.randomUUID().toString().substring(0,8).toUpperCase());
        
        payment = paymentRepository.save(payment);

        BigDecimal remainingToAllocate = payment.getUnallocatedAmount();

        if (request.getFeeDueIds() != null && !request.getFeeDueIds().isEmpty()) {
            List<FeeDue> dues = feeDueRepository.findAllById(request.getFeeDueIds());
            
            // Sort dues by due date manually or rely on a query. We already fetched by ID so we should sort.
            dues.sort((d1, d2) -> d1.getDueDate().compareTo(d2.getDueDate()));

            for (FeeDue due : dues) {
                if (remainingToAllocate.compareTo(BigDecimal.ZERO) <= 0) break;
                
                if ("PAID".equals(due.getStatus())) continue;

                BigDecimal amountNeeded = due.getAmount().subtract(due.getPaidAmount());
                
                if (amountNeeded.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal allocated = amountNeeded.min(remainingToAllocate);
                    
                    due.setPaidAmount(due.getPaidAmount().add(allocated));
                    if (due.getPaidAmount().compareTo(due.getAmount()) >= 0) {
                        due.setStatus("PAID");
                    } else {
                        due.setStatus("PARTIAL");
                    }
                    feeDueRepository.save(due);
                    
                    FeePaymentAllocation allocation = new FeePaymentAllocation();
                    allocation.setPayment(payment);
                    allocation.setFeeDue(due);
                    allocation.setAllocatedAmount(allocated);
                    feePaymentAllocationRepository.save(allocation);
                    
                    remainingToAllocate = remainingToAllocate.subtract(allocated);
                }
            }
            
            payment.setUnallocatedAmount(remainingToAllocate);
            paymentRepository.save(payment);
        }

        return new PaymentResponse(payment.getId(), school.getId(), payment.getAmount(), payment.getPaymentMode(), payment.getTransactionId(), payment.getStatus(), payment.getCreatedAt());
    }
}
