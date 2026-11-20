package com.school.erp.service.finance.impl;

import com.school.erp.dto.payment.FeeReminderResponse;
import com.school.erp.dto.payment.PaymentResponse;
import com.school.erp.dto.payment.StudentFeeRecordDTO;
import com.school.erp.entity.academic.SchoolClass;
import com.school.erp.entity.academic.Section;
import com.school.erp.entity.auth.UserRole;
import com.school.erp.entity.finance.FeeDue;
import com.school.erp.entity.finance.FeePaymentAllocation;
import com.school.erp.entity.finance.FeeReminder;
import com.school.erp.entity.finance.Payment;
import com.school.erp.entity.hr.Staff;
import com.school.erp.entity.student.Student;
import com.school.erp.entity.superadmin.School;
import com.school.erp.repository.academic.ClassTeacherAssignmentRepository;
import com.school.erp.repository.academic.SchoolClassRepository;
import com.school.erp.repository.academic.SectionRepository;
import com.school.erp.repository.auth.UserAssignmentRepository;
import com.school.erp.repository.finance.FeeDueRepository;
import com.school.erp.repository.finance.FeePaymentAllocationRepository;
import com.school.erp.repository.finance.FeeReminderRepository;
import com.school.erp.repository.finance.PaymentRepository;
import com.school.erp.repository.finance.StudentFeeAggregation;
import com.school.erp.repository.hr.StaffRepository;
import com.school.erp.repository.student.StudentRepository;
import com.school.erp.repository.superadmin.SchoolRepository;
import com.school.erp.security.AuthContextService;
import com.school.erp.security.AuthenticatedUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FeePagesAuditFixesTest {

    @Mock private ClassTeacherAssignmentRepository classTeacherAssignmentRepository;
    @Mock private StaffRepository staffRepository;
    @Mock private FeeReminderRepository feeReminderRepository;
    @Mock private SchoolRepository schoolRepository;
    @Mock private StudentRepository studentRepository;
    @Mock private AuthContextService authContextService;
    @Mock private UserAssignmentRepository userAssignmentRepository;
    @Mock private SchoolClassRepository schoolClassRepository;
    @Mock private SectionRepository sectionRepository;
    @Mock private FeeDueRepository feeDueRepository;
    @Mock private PaymentRepository paymentRepository;
    @Mock private FeePaymentAllocationRepository feePaymentAllocationRepository;

    @Test
    void reminderHistory_adminWithStaffRecordSeesAllSchoolRemindersOrderedDesc() {
        TeacherFeeServiceImpl service = new TeacherFeeServiceImpl(
                classTeacherAssignmentRepository,
                staffRepository,
                feeReminderRepository,
                schoolRepository,
                studentRepository,
                authContextService,
                userAssignmentRepository,
                schoolClassRepository,
                sectionRepository,
                feeDueRepository
        );

        AuthenticatedUser adminUser = new AuthenticatedUser(10L, 1000L, UserRole.ADMIN);
        when(authContextService.requireCurrentUser()).thenReturn(adminUser);

        Staff adminStaff = new Staff();
        adminStaff.setId(55L);
        adminStaff.setFirstName("Priya");
        adminStaff.setLastName("Mehta");
        when(staffRepository.findByUserId(10L)).thenReturn(Optional.of(adminStaff));

        SchoolClass grade10 = new SchoolClass();
        grade10.setId(2L);
        grade10.setName("Grade 10");

        Section secA = new Section();
        secA.setId(5L);
        secA.setName("A");
        when(sectionRepository.findBySchoolId(1000L)).thenReturn(List.of(secA));

        Student student = new Student();
        student.setId(101L);
        student.setFirstName("Aarav");
        student.setLastName("Sharma");
        student.setGuardianName("Rajesh Sharma");
        student.setSchoolClass(grade10);
        student.setSectionId(5L);

        FeeReminder reminder = new FeeReminder();
        reminder.setId(901L);
        reminder.setStudent(student);
        reminder.setStaff(adminStaff);
        reminder.setFeeAmount(new BigDecimal("12000.00"));
        reminder.setMethod("APP");
        reminder.setStatus("SENT");
        reminder.setSentAt(LocalDateTime.of(2026, 11, 19, 10, 0));

        PageRequest pageable = PageRequest.of(0, 20);
        when(feeReminderRepository.findBySchoolIdOrderBySentAtDesc(1000L, pageable))
                .thenReturn(new PageImpl<>(List.of(reminder), pageable, 1));

        Page<FeeReminderResponse> page = service.getReminderHistory(1000L, pageable);

        assertEquals(1, page.getTotalElements());
        FeeReminderResponse item = page.getContent().get(0);
        assertEquals("Aarav Sharma", item.getStudentName());
        assertEquals("Grade 10", item.getClassName());
        assertEquals("A", item.getSectionName());
        assertEquals("Priya Mehta", item.getSentBy());
        assertEquals(new BigDecimal("12000.00"), item.getFeeAmount());

        verify(feeReminderRepository).findBySchoolIdOrderBySentAtDesc(1000L, pageable);
        verify(feeReminderRepository, never()).findBySchoolIdAndStaffIdOrderBySentAtDesc(any(), any(), any());
    }

    @Test
    void paymentsBySchool_usesDescOrderingAndBatchAllocationLookupWithSection() {
        FeePaymentServiceImpl service = new FeePaymentServiceImpl(
                paymentRepository,
                feeDueRepository,
                feePaymentAllocationRepository,
                studentRepository,
                schoolRepository,
                sectionRepository,
                null,
                null,
                null
        );

        School school = new School();
        school.setId(1000L);

        SchoolClass grade10 = new SchoolClass();
        grade10.setId(2L);
        grade10.setName("Grade 10");

        Section secB = new Section();
        secB.setId(8L);
        secB.setName("B");
        when(sectionRepository.findBySchoolId(1000L)).thenReturn(List.of(secB));

        Student student = new Student();
        student.setId(101L);
        student.setFirstName("Aarav");
        student.setLastName("Sharma");
        student.setSchoolClass(grade10);
        student.setSectionId(8L);

        Payment p1 = new Payment();
        p1.setId(501L);
        p1.setSchool(school);
        p1.setStudent(student);
        p1.setAmount(new BigDecimal("15000.00"));
        p1.setPaymentMode("CASH");
        p1.setReceiptNumber("REC-2026-00501");
        p1.setPaymentDate(LocalDate.of(2026, 11, 19));
        p1.setCreatedAt(LocalDateTime.of(2026, 11, 19, 10, 15));
        p1.setStatus("COMPLETED");

        FeeDue due = new FeeDue();
        due.setId(77L);
        due.setTitle("Term 1");

        FeePaymentAllocation alloc = new FeePaymentAllocation();
        alloc.setPayment(p1);
        alloc.setFeeDue(due);
        alloc.setAllocatedAmount(new BigDecimal("15000.00"));

        PageRequest pageable = PageRequest.of(0, 15);
        when(paymentRepository.findBySchoolIdOrderByCreatedAtDescIdDesc(1000L, pageable))
                .thenReturn(new PageImpl<>(List.of(p1), pageable, 1));
        when(feePaymentAllocationRepository.findByPaymentIdIn(List.of(501L)))
                .thenReturn(List.of(alloc));

        Page<PaymentResponse> result = service.getPaymentsBySchool(1000L, null, pageable);

        assertEquals(1, result.getTotalElements());
        PaymentResponse resp = result.getContent().get(0);
        assertEquals("Aarav Sharma", resp.studentName());
        assertEquals("Grade 10 - Section B", resp.className());
        assertEquals("REC-2026-00501", resp.receiptNumber());
        assertEquals(1, resp.allocations().size());

        verify(feePaymentAllocationRepository).findByPaymentIdIn(List.of(501L));
        verify(feePaymentAllocationRepository, never()).findByPaymentId(any());
    }

    @Test
    void feeRecords_derivesOverdueStatusAndPopulatesSectionName() {
        FeeRecordServiceImpl service = new FeeRecordServiceImpl(
                studentRepository,
                feeDueRepository,
                authContextService,
                null,
                null
        );
        ReflectionTestUtils.setField(service, "sectionRepository", sectionRepository);

        AuthenticatedUser superAdmin = new AuthenticatedUser(1L, 1000L, UserRole.SUPER_ADMIN);
        when(authContextService.getCurrentUserOrNull()).thenReturn(superAdmin);
        when(authContextService.resolveSchoolId(1000L)).thenReturn(1000L);

        SchoolClass grade10 = new SchoolClass();
        grade10.setId(2L);
        grade10.setName("Grade 10");

        Section secC = new Section();
        secC.setId(9L);
        secC.setName("C");
        when(sectionRepository.findBySchoolId(1000L)).thenReturn(List.of(secC));

        Student student = new Student();
        student.setId(202L);
        student.setFirstName("Palak");
        student.setLastName("Verma");
        student.setAdmissionNo("ADM-2026-202");
        student.setSchoolClass(grade10);
        student.setSectionId(9L);

        PageRequest pageable = PageRequest.of(0, 50);
        when(studentRepository.findBySchoolIdAndSearchAndClassIdAndSectionId(1000L, "", 2L, 9L, pageable))
                .thenReturn(new PageImpl<>(List.of(student), pageable, 1));

        StudentFeeAggregation agg = new StudentFeeAggregation() {
            @Override public Long getStudentId() { return 202L; }
            @Override public BigDecimal getTotalFees() { return new BigDecimal("20000.00"); }
            @Override public BigDecimal getAmountPaid() { return new BigDecimal("5000.00"); }
            @Override public BigDecimal getDiscountAmount() { return BigDecimal.ZERO; }
        };
        when(feeDueRepository.aggregateFeesByStudentIds(List.of(202L))).thenReturn(List.of(agg));
        when(feeDueRepository.findOverdueStudentIds(eq(List.of(202L)), any(LocalDate.class))).thenReturn(List.of(202L));

        Page<StudentFeeRecordDTO> page = service.getStudentFeeRecords(1000L, 2L, 9L, "", null, pageable);

        assertEquals(1, page.getTotalElements());
        StudentFeeRecordDTO dto = page.getContent().get(0);
        assertEquals("OVERDUE", dto.getStatus());
        assertEquals("C", dto.getSectionName());
        assertEquals(new BigDecimal("15000.00"), dto.getBalance());
    }
}
