package com.school.erp.service;

import com.school.erp.dto.payment.PaymentRequest;
import com.school.erp.dto.payment.PaymentResponse;
import com.school.erp.entity.FeeInvoice;
import com.school.erp.entity.Payment;
import com.school.erp.entity.School;
import com.school.erp.exception.ResourceNotFoundException;
import com.school.erp.repository.FeeInvoiceRepository;
import com.school.erp.repository.PaymentRepository;
import com.school.erp.repository.SchoolRepository;
import com.school.erp.security.AuthContextService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final FeeInvoiceRepository feeInvoiceRepository;
    private final SchoolRepository schoolRepository;
    private final AuthContextService authContextService;

    public PaymentService(
            PaymentRepository paymentRepository,
            FeeInvoiceRepository feeInvoiceRepository,
            SchoolRepository schoolRepository,
            AuthContextService authContextService
    ) {
        this.paymentRepository = paymentRepository;
        this.feeInvoiceRepository = feeInvoiceRepository;
        this.schoolRepository = schoolRepository;
        this.authContextService = authContextService;
    }

    public List<PaymentResponse> getAllPayments(Long schoolId, Long invoiceId) {
        Long effectiveSchoolId = authContextService.resolveSchoolId(schoolId);
        List<Payment> payments = invoiceId == null
                ? paymentRepository.findBySchoolId(effectiveSchoolId)
                : paymentRepository.findBySchoolIdAndInvoiceId(effectiveSchoolId, invoiceId);
        return payments.stream().map(this::toResponse).toList();
    }

    @Transactional
    public PaymentResponse createPayment(PaymentRequest request) {
        Long effectiveSchoolId = authContextService.resolveSchoolId(request.schoolId());
        School school = getSchool(effectiveSchoolId);
        FeeInvoice invoice = getInvoice(request.invoiceId(), effectiveSchoolId);
        Payment payment = new Payment();
        payment.setSchool(school);
        payment.setInvoice(invoice);
        payment.setAmount(request.amount());
        payment.setPaymentMode(request.paymentMode());
        payment.setTransactionId(request.transactionId());
        payment.setStatus(request.status());
        return toResponse(paymentRepository.save(payment));
    }

    private School getSchool(Long schoolId) {
        return schoolRepository.findById(schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("School not found for id " + schoolId));
    }

    private FeeInvoice getInvoice(Long invoiceId, Long schoolId) {
        return feeInvoiceRepository.findByIdAndSchoolId(invoiceId, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Invoice not found for id " + invoiceId + " and schoolId " + schoolId
                ));
    }

    private PaymentResponse toResponse(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getInvoice().getId(),
                payment.getSchool().getId(),
                payment.getAmount(),
                payment.getPaymentMode(),
                payment.getTransactionId(),
                payment.getStatus(),
                payment.getCreatedAt()
        );
    }
}
