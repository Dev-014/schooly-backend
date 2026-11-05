package com.school.erp.service.finance;

import com.school.erp.dto.payment.FeeDueResponse;
import com.school.erp.dto.payment.FeePaymentRequest;
import com.school.erp.dto.payment.PaymentResponse;
import com.school.erp.entity.finance.FeeCategory;
import com.school.erp.entity.finance.FeeDue;
import com.school.erp.entity.finance.Payment;
import com.school.erp.entity.student.Student;
import com.school.erp.entity.superadmin.School;
import com.school.erp.repository.finance.*;
import com.school.erp.repository.student.StudentRepository;
import com.school.erp.repository.superadmin.SchoolRepository;
import com.school.erp.service.finance.impl.FeeDueServiceImpl;
import com.school.erp.service.finance.impl.FeePaymentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FeeDiscountAndPaymentTest {

    @Mock
    private FeeDueRepository feeDueRepository;

    @Mock
    private FeeCategoryRepository feeCategoryRepository;

    @Mock
    private SchoolRepository schoolRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private FeeStructureRepository feeStructureRepository;

    @Mock
    private StudentFeeStructureRepository studentFeeStructureRepository;

    @Mock
    private FeeGenerationBatchRepository feeGenerationBatchRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private FeePaymentAllocationRepository feePaymentAllocationRepository;

    private FeeDueServiceImpl feeDueService;
    private FeePaymentServiceImpl feePaymentService;

    private School testSchool;
    private Student testStudent;

    @BeforeEach
    void setUp() {
        feeDueService = new FeeDueServiceImpl(
                feeDueRepository,
                feeCategoryRepository,
                schoolRepository,
                studentRepository,
                feeStructureRepository,
                studentFeeStructureRepository,
                feeGenerationBatchRepository
        );

        feePaymentService = new FeePaymentServiceImpl(
                paymentRepository,
                feeDueRepository,
                feePaymentAllocationRepository,
                studentRepository,
                schoolRepository
        );

        testSchool = new School();
        testSchool.setId(1000L);

        testStudent = new Student();
        testStudent.setId(23L);
        testStudent.setName("Paras Patidar");
        testStudent.setSchool(testSchool);
    }

    @Test
    void testGetStudentDues_marksDueWithDiscountAsPaid() {
        FeeDue due = new FeeDue();
        due.setId(40L);
        due.setTitle("Second Installment");
        due.setAmount(new BigDecimal("15000.00"));
        due.setPaidAmount(new BigDecimal("14400.00"));
        due.setDiscountAmount(new BigDecimal("600.00"));
        due.setStatus("PARTIAL"); // Database had legacy status 'PARTIAL'
        due.setDueDate(LocalDate.of(2026, 11, 26));
        due.setStudent(testStudent);
        due.setSchool(testSchool);

        when(feeDueRepository.findByStudentIdAndSchoolIdOrderByDueDateAsc(23L, 1000L))
                .thenReturn(List.of(due));

        List<FeeDueResponse> response = feeDueService.getStudentDues(23L, 1000L, null);

        assertNotNull(response);
        assertEquals(1, response.size());
        FeeDueResponse firstDue = response.get(0);
        assertEquals(40L, firstDue.id());
        assertEquals("PAID", firstDue.status());
        assertEquals(new BigDecimal("15000.00"), firstDue.amount());
        assertEquals(new BigDecimal("14400.00"), firstDue.paidAmount());
        assertEquals(new BigDecimal("600.00"), firstDue.discountAmount());
    }

    @Test
    void testProcessPayment_calculatesAmountNeededWithDiscountAndSetsPaidStatus() {
        FeeDue due = new FeeDue();
        due.setId(40L);
        due.setTitle("Second Installment");
        due.setAmount(new BigDecimal("15000.00"));
        due.setPaidAmount(BigDecimal.ZERO);
        due.setDiscountAmount(new BigDecimal("600.00"));
        due.setStatus("UNPAID");
        due.setDueDate(LocalDate.of(2026, 11, 26));
        due.setStudent(testStudent);
        due.setSchool(testSchool);

        when(schoolRepository.findById(1000L)).thenReturn(Optional.of(testSchool));
        when(studentRepository.findById(23L)).thenReturn(Optional.of(testStudent));
        when(feeDueRepository.findAllById(List.of(40L))).thenReturn(new java.util.ArrayList<>(List.of(due)));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FeePaymentRequest request = new FeePaymentRequest();
        request.setSchoolId(1000L);
        request.setStudentId(23L);
        request.setFeeDueIds(List.of(40L));
        request.setTotalAmount(new BigDecimal("14400.00"));
        request.setPaymentDate(LocalDate.of(2026, 11, 5));
        request.setPaymentMethod("CASH");

        PaymentResponse response = feePaymentService.processPayment(request);

        assertNotNull(response);
        assertEquals("PAID", due.getStatus());
        assertEquals(new BigDecimal("14400.00"), due.getPaidAmount());
        assertEquals(new BigDecimal("600.00"), due.getDiscountAmount());
        assertEquals(new BigDecimal("14400.00"), response.amount());
        assertEquals(1, response.allocations().size());
        assertEquals(new BigDecimal("14400.00"), response.allocations().get(0).allocatedAmount());
        verify(feeDueRepository).save(due);
    }
}
