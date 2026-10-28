package com.school.erp.controller.finance;

import com.school.erp.dto.payment.FeeDueResponse;
import com.school.erp.dto.payment.PaymentResponse;
import com.school.erp.entity.superadmin.School;
import com.school.erp.entity.auth.UserRole;
import com.school.erp.entity.student.Student;
import com.school.erp.entity.student.StudentParent;
import com.school.erp.entity.student.StudentParentId;
import com.school.erp.repository.student.StudentParentRepository;
import com.school.erp.repository.student.StudentRepository;
import com.school.erp.security.AuthContextHolder;
import com.school.erp.security.AuthFilterConfig;
import com.school.erp.security.AuthenticatedUser;
import com.school.erp.security.JwtAuthenticationFilter;
import com.school.erp.service.finance.FeeDueService;
import com.school.erp.service.finance.FeePaymentService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = StudentFeeController.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class),
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = AuthFilterConfig.class)
        }
)
class StudentFeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FeeDueService feeDueService;

    @MockitoBean
    private FeePaymentService feePaymentService;

    @MockitoBean
    private StudentRepository studentRepository;

    @MockitoBean
    private StudentParentRepository studentParentRepository;

    private School mockSchool;
    private Student mockStudent;

    @BeforeEach
    void setUp() {
        mockSchool = new School();
        mockSchool.setId(1L);
        mockSchool.setName("Demo School");

        mockStudent = new Student();
        mockStudent.setId(101L);
        mockStudent.setSchool(mockSchool);
        mockStudent.setFirstName("Rahul");
        mockStudent.setLastName("Sharma");
    }

    @AfterEach
    void tearDown() {
        AuthContextHolder.clear();
    }

    @Test
    void student_GetMySummary_Success() throws Exception {
        AuthContextHolder.set(new AuthenticatedUser(50L, 1L, UserRole.STUDENT));
        when(studentRepository.findByUserId(50L)).thenReturn(List.of(mockStudent));

        FeeDueResponse due = new FeeDueResponse(
                1L, "Term 1 Tuition",
                new BigDecimal("15000.00"), new BigDecimal("5000.00"), BigDecimal.ZERO,
                LocalDate.now().plusDays(10), "PARTIAL", 1L, "Tuition", "Term 1"
        );
        when(feeDueService.getStudentDues(101L, 1L, null)).thenReturn(List.of(due));

        mockMvc.perform(get("/api/v1/student/fees/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.totalFee").value(15000.00))
                .andExpect(jsonPath("$.data.totalPaid").value(5000.00))
                .andExpect(jsonPath("$.data.pendingAmount").value(10000.00))
                .andExpect(jsonPath("$.data.overallStatus").value("PARTIALLY_PAID"));
    }

    @Test
    void parent_GetChildDues_Success() throws Exception {
        AuthContextHolder.set(new AuthenticatedUser(200L, 1L, UserRole.PARENT));

        StudentParent sp = new StudentParent();
        sp.setId(new StudentParentId(200L, 101L));
        sp.setStudent(mockStudent);

        when(studentParentRepository.findByIdParentUserId(200L)).thenReturn(List.of(sp));

        FeeDueResponse due = new FeeDueResponse(
                1L, "Term 1 Tuition",
                new BigDecimal("15000.00"), BigDecimal.ZERO, BigDecimal.ZERO,
                LocalDate.now().plusDays(10), "PENDING", 1L, "Tuition", "Term 1"
        );
        when(feeDueService.getStudentDues(101L, 1L, null)).thenReturn(List.of(due));

        mockMvc.perform(get("/api/v1/student/fees/dues").param("studentId", "101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].title").value("Term 1 Tuition"));
    }

    @Test
    void parent_GetChildPayments_Success() throws Exception {
        AuthContextHolder.set(new AuthenticatedUser(200L, 1L, UserRole.PARENT));

        StudentParent sp = new StudentParent();
        sp.setId(new StudentParentId(200L, 101L));
        sp.setStudent(mockStudent);

        when(studentParentRepository.findByIdParentUserId(200L)).thenReturn(List.of(sp));

        PaymentResponse payment = new PaymentResponse(
                10L, 1L, new BigDecimal("5000.00"), "ONLINE", "TXN-999", "SUCCESS",
                "RCP-2026-001", "Rahul Sharma", "10th A",
                LocalDate.now(), LocalDateTime.now(), Collections.emptyList()
        );
        when(feePaymentService.getPaymentsByStudent(eq(101L), eq(1L), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(payment)));

        mockMvc.perform(get("/api/v1/student/fees/payments").param("studentId", "101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.content[0].receiptNumber").value("RCP-2026-001"))
                .andExpect(jsonPath("$.data.content[0].amount").value(5000.00));
    }
}
