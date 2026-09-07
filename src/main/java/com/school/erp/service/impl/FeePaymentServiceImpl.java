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
import java.util.ArrayList;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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
        List<PaymentResponse.PaymentAllocationResponse> allocations = new ArrayList<>();

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
                    
                    allocations.add(new PaymentResponse.PaymentAllocationResponse(due.getId(), due.getTitle(), allocated));

                    remainingToAllocate = remainingToAllocate.subtract(allocated);
                }
            }
            
            payment.setUnallocatedAmount(remainingToAllocate);
            paymentRepository.save(payment);
        }

        return new PaymentResponse(
            payment.getId(), 
            school.getId(), 
            payment.getAmount(), 
            payment.getPaymentMode(), 
            payment.getTransactionId(), 
            payment.getStatus(), 
            payment.getReceiptNumber(),
            student.getFirstName() + " " + (student.getLastName() != null ? student.getLastName() : ""),
            student.getSchoolClass() != null ? student.getSchoolClass().getName() : "",
            payment.getPaymentDate(),
            payment.getCreatedAt(),
            allocations
        );
    }

    @Override
    public Page<PaymentResponse> getPaymentsBySchool(Long schoolId, String search, Pageable pageable) {
        Page<Payment> payments;
        if (search != null && !search.trim().isEmpty()) {
            payments = paymentRepository.findBySchoolIdAndSearch(schoolId, search, pageable);
        } else {
            payments = paymentRepository.findBySchoolId(schoolId, pageable);
        }

        return payments.map(payment -> {
            List<PaymentResponse.PaymentAllocationResponse> allocations = feePaymentAllocationRepository.findByPaymentId(payment.getId())
                .stream()
                .map(alloc -> new PaymentResponse.PaymentAllocationResponse(
                    alloc.getFeeDue().getId(),
                    alloc.getFeeDue().getTitle(),
                    alloc.getAllocatedAmount()
                )).toList();

            return new PaymentResponse(
                payment.getId(),
                payment.getSchool().getId(),
                payment.getAmount(),
                payment.getPaymentMode(),
                payment.getTransactionId(),
                payment.getStatus(),
                payment.getReceiptNumber(),
                payment.getStudent().getFirstName() + " " + (payment.getStudent().getLastName() != null ? payment.getStudent().getLastName() : ""),
                payment.getStudent().getSchoolClass() != null ? payment.getStudent().getSchoolClass().getName() : "",
                payment.getPaymentDate(),
                payment.getCreatedAt(),
                allocations
            );
        });
    }

    @Override
    public Page<PaymentResponse> getPaymentsByStudent(Long studentId, Long schoolId, Pageable pageable) {
        Page<Payment> payments = paymentRepository.findBySchoolIdAndStudentId(schoolId, studentId, pageable);

        return payments.map(payment -> {
            List<PaymentResponse.PaymentAllocationResponse> allocations = feePaymentAllocationRepository.findByPaymentId(payment.getId())
                .stream()
                .map(alloc -> new PaymentResponse.PaymentAllocationResponse(
                    alloc.getFeeDue().getId(),
                    alloc.getFeeDue().getTitle(),
                    alloc.getAllocatedAmount()
                )).toList();

            return new PaymentResponse(
                payment.getId(),
                payment.getSchool().getId(),
                payment.getAmount(),
                payment.getPaymentMode(),
                payment.getTransactionId(),
                payment.getStatus(),
                payment.getReceiptNumber(),
                payment.getStudent().getFirstName() + " " + (payment.getStudent().getLastName() != null ? payment.getStudent().getLastName() : ""),
                payment.getStudent().getSchoolClass() != null ? payment.getStudent().getSchoolClass().getName() : "",
                payment.getPaymentDate(),
                payment.getCreatedAt(),
                allocations
            );
        });
    }
}
