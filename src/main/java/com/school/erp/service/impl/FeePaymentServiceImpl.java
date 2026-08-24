package com.school.erp.service.impl;

import com.school.erp.dto.payment.FeePaymentRequest;
import com.school.erp.dto.payment.PaymentResponse;
import com.school.erp.entity.*;
import com.school.erp.repository.*;
import com.school.erp.service.FeePaymentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@Transactional
public class FeePaymentServiceImpl implements FeePaymentService {

    private final PaymentRepository paymentRepository;
    private final FeeInvoiceRepository feeInvoiceRepository;
    private final FeePaymentItemRepository feePaymentItemRepository;
    private final FeeInvoiceItemRepository feeInvoiceItemRepository;
    private final StudentRepository studentRepository;
    private final SchoolRepository schoolRepository;

    public FeePaymentServiceImpl(PaymentRepository paymentRepository, FeeInvoiceRepository feeInvoiceRepository, FeePaymentItemRepository feePaymentItemRepository, FeeInvoiceItemRepository feeInvoiceItemRepository, StudentRepository studentRepository, SchoolRepository schoolRepository) {
        this.paymentRepository = paymentRepository;
        this.feeInvoiceRepository = feeInvoiceRepository;
        this.feePaymentItemRepository = feePaymentItemRepository;
        this.feeInvoiceItemRepository = feeInvoiceItemRepository;
        this.studentRepository = studentRepository;
        this.schoolRepository = schoolRepository;
    }

    @Override
    public PaymentResponse processPayment(FeePaymentRequest request) {
        School school = schoolRepository.findById(request.getSchoolId()).orElseThrow(() -> new RuntimeException("School not found"));
        Student student = studentRepository.findById(request.getStudentId()).orElseThrow(() -> new RuntimeException("Student not found"));
        FeeInvoice invoice = feeInvoiceRepository.findById(request.getInvoiceId()).orElseThrow(() -> new RuntimeException("Invoice not found"));

        Payment payment = new Payment();
        payment.setSchool(school);
        payment.setStudent(student);
        payment.setInvoice(invoice);
        payment.setAmount(request.getTotalAmount());
        payment.setPaymentMode(request.getPaymentMethod());
        payment.setPaymentDate(request.getPaymentDate());
        payment.setStatus("SUCCESS");
        payment.setReceiptNumber("REC-" + UUID.randomUUID().toString().substring(0,8).toUpperCase());
        
        payment = paymentRepository.save(payment);

        BigDecimal totalPaid = BigDecimal.ZERO;
        
        for (Long itemId : request.getFeeItemIds()) {
            FeeInvoiceItem invoiceItem = feeInvoiceItemRepository.findById(itemId).orElseThrow(() -> new RuntimeException("Invoice item not found"));
            
            BigDecimal amountToPay = invoiceItem.getAmount().subtract(invoiceItem.getPaidAmount());
            if (amountToPay.compareTo(BigDecimal.ZERO) > 0) {
                invoiceItem.setPaidAmount(invoiceItem.getPaidAmount().add(amountToPay));
                feeInvoiceItemRepository.save(invoiceItem);
                
                FeePaymentItem pItem = new FeePaymentItem();
                pItem.setPayment(payment);
                pItem.setFeeInvoiceItem(invoiceItem);
                pItem.setAmount(amountToPay);
                feePaymentItemRepository.save(pItem);
                
                totalPaid = totalPaid.add(amountToPay);
            }
        }

        invoice.setPaidAmount(invoice.getPaidAmount().add(totalPaid));
        if (invoice.getPaidAmount().compareTo(invoice.getTotalAmount()) >= 0) {
            invoice.setStatus("PAID");
        } else {
            invoice.setStatus("PARTIAL");
        }
        feeInvoiceRepository.save(invoice);

        return new PaymentResponse(payment.getId(), invoice.getId(), school.getId(), payment.getAmount(), payment.getPaymentMode(), payment.getTransactionId(), payment.getStatus(), payment.getCreatedAt());
    }
}
