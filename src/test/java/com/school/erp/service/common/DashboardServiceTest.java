package com.school.erp.service.common;

import com.school.erp.dto.dashboard.CollectionExpensePointResponse;
import com.school.erp.dto.dashboard.DashboardBreakdownResponse;
import com.school.erp.dto.dashboard.DashboardKpiResponse;
import com.school.erp.entity.academic.SchoolClass;
import com.school.erp.entity.student.Student;
import com.school.erp.repository.accounting.ExpenseRepository;
import com.school.erp.repository.accounting.IncomeRepository;
import com.school.erp.repository.attendance.AttendanceRepository;
import com.school.erp.repository.finance.FeeDueRepository;
import com.school.erp.repository.finance.FeeStructureItemRepository;
import com.school.erp.repository.finance.PaymentRepository;
import com.school.erp.repository.hr.StaffAttendanceRepository;
import com.school.erp.repository.hr.StaffRepository;
import com.school.erp.repository.student.StudentRepository;
import com.school.erp.security.AuthContextService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private StudentRepository studentRepository;
    @Mock
    private StaffRepository staffRepository;
    @Mock
    private AttendanceRepository attendanceRepository;
    @Mock
    private StaffAttendanceRepository staffAttendanceRepository;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private ExpenseRepository expenseRepository;
    @Mock
    private IncomeRepository incomeRepository;
    @Mock
    private FeeDueRepository feeDueRepository;
    @Mock
    private FeeStructureItemRepository feeStructureItemRepository;
    @Mock
    private AuthContextService authContextService;

    @InjectMocks
    private DashboardService dashboardService;

    @BeforeEach
    void setUp() {
        when(authContextService.resolveSchoolId(any())).thenAnswer(inv -> inv.getArgument(0) != null ? inv.getArgument(0) : 1L);
    }

    @Test
    void testGetKpisCalculatesAccurately() {
        when(studentRepository.countBySchoolId(1L)).thenReturn(500L);
        when(staffRepository.countBySchoolId(1L)).thenReturn(40L);
        when(attendanceRepository.countBySchoolIdAndAttendanceDateAndStatus(eq(1L), any(), eq("PRESENT"))).thenReturn(470L);
        when(staffAttendanceRepository.countBySchoolIdAndAttendanceDateAndStatus(eq(1L), any(), eq("PRESENT"))).thenReturn(38L);
        when(paymentRepository.sumAmountBySchoolAndMonthAndYear(eq(1L), anyInt(), anyInt())).thenReturn(new BigDecimal("150000.00"));
        when(expenseRepository.sumAmountBySchoolAndMonthAndYear(eq(1L), anyInt(), anyInt())).thenReturn(new BigDecimal("45000.00"));
        when(incomeRepository.sumAmountBySchoolAndMonthAndYear(eq(1L), anyInt(), anyInt())).thenReturn(new BigDecimal("30000.00"));
        when(studentRepository.countNewAdmissionsBySchoolAndMonthAndYear(eq(1L), anyInt(), anyInt())).thenReturn(25L);

        DashboardKpiResponse response = dashboardService.getKpis(1L);

        assertNotNull(response);
        assertEquals(new BigDecimal("150000.00"), response.monthlyCollection());
        assertEquals(new BigDecimal("45000.00"), response.monthlyExpense());
        assertEquals(new BigDecimal("30000.00"), response.monthlyIncome());
        assertEquals(500L, response.studentTotal());
        assertEquals(470L, response.studentPresent());
        assertEquals(40L, response.staffTotal());
        assertEquals(38L, response.staffPresent());
        assertEquals(25L, response.newAdmissions());
    }

    @Test
    void testGetCollectionVsExpenseBuilds12Months() {
        List<Object[]> collections = List.<Object[]>of(new Object[]{1, new BigDecimal("50000.00")});
        List<Object[]> incomes = List.<Object[]>of(new Object[]{1, new BigDecimal("10000.00")});
        List<Object[]> expenses = List.<Object[]>of(new Object[]{1, new BigDecimal("20000.00")});
        when(paymentRepository.sumMonthlyAmountBySchoolAndYear(1L, 2026)).thenReturn(collections);
        when(incomeRepository.sumMonthlyAmountBySchoolAndYear(1L, 2026)).thenReturn(incomes);
        when(expenseRepository.sumMonthlyAmountBySchoolAndYear(1L, 2026)).thenReturn(expenses);

        List<CollectionExpensePointResponse> points = dashboardService.getCollectionVsExpense(1L, 2026);

        assertEquals(12, points.size());
        assertEquals("JAN", points.get(0).month());
        assertEquals(new BigDecimal("60000.00"), points.get(0).collected());
        assertEquals(new BigDecimal("20000.00"), points.get(0).expense());
    }

    @Test
    void testGetEarningModesCalculatesPercentages() {
        List<Object[]> rows = List.<Object[]>of(
                new Object[]{"ONLINE", new BigDecimal("80000.00"), 50L},
                new Object[]{"CASH", new BigDecimal("20000.00"), 20L}
        );
        when(paymentRepository.sumAmountByPaymentMode(1L)).thenReturn(rows);

        List<DashboardBreakdownResponse.EarningModeItem> modes = dashboardService.getEarningModes(1L);

        assertEquals(2, modes.size());
        assertEquals("Online", modes.get(0).mode());
        assertEquals(80.0, modes.get(0).percentage());
        assertEquals("Cash/Other", modes.get(1).mode());
        assertEquals(20.0, modes.get(1).percentage());
    }

    @Test
    void testGetFeeDistributionCalculatesProportions() {
        List<Object[]> rows = List.<Object[]>of(
                new Object[]{"Tuition Fee", new BigDecimal("60000.00")},
                new Object[]{"Transport Fee", new BigDecimal("40000.00")}
        );
        when(feeStructureItemRepository.sumAmountsByCategoryAndSchoolId(1L)).thenReturn(rows);

        List<DashboardBreakdownResponse.FeeDistributionItem> dist = dashboardService.getFeeDistribution(1L);

        assertEquals(2, dist.size());
        assertEquals("Tuition Fee", dist.get(0).category());
        assertEquals(60.0, dist.get(0).percentage());
        assertEquals("Transport Fee", dist.get(1).category());
        assertEquals(40.0, dist.get(1).percentage());
    }

    @Test
    void testGetClassDistributionCalculatesStudentCounts() {
        List<Object[]> rows = List.<Object[]>of(
                new Object[]{10L, "Grade 10", 30L},
                new Object[]{11L, "Grade 11", 20L}
        );
        when(studentRepository.countStudentsPerClass(1L)).thenReturn(rows);
        when(studentRepository.countBySchoolId(1L)).thenReturn(50L);

        List<DashboardBreakdownResponse.ClassDistributionItem> dist = dashboardService.getClassDistribution(1L);

        assertEquals(2, dist.size());
        assertEquals("Grade 10", dist.get(0).className());
        assertEquals(30L, dist.get(0).studentCount());
        assertEquals(60.0, dist.get(0).percentage());
    }

    @Test
    void testGetStudentBirthdaysReturnsStudents() {
        Student s = new Student();
        s.setId(101L);
        s.setFirstName("Alex");
        s.setLastName("Mercer");
        s.setDateOfBirth(LocalDate.now());
        SchoolClass c = new SchoolClass();
        c.setName("Class 10-A");
        s.setSchoolClass(c);

        when(studentRepository.findStudentsWithBirthdayToday(eq(1L), anyInt(), anyInt())).thenReturn(List.of(s));

        List<DashboardBreakdownResponse.StudentBirthdayItem> birthdays = dashboardService.getStudentBirthdays(1L);

        assertEquals(1, birthdays.size());
        assertEquals("Alex Mercer", birthdays.get(0).studentName());
        assertEquals("Class 10-A", birthdays.get(0).className());
    }
}
