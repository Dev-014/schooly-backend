package com.school.erp.service.common;

import com.school.erp.dto.dashboard.ActivityFeedResponse;
import com.school.erp.dto.dashboard.CollectionExpensePointResponse;
import com.school.erp.dto.dashboard.DashboardBreakdownResponse;
import com.school.erp.dto.dashboard.DashboardKpiResponse;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    private final StudentRepository studentRepository;
    private final StaffRepository staffRepository;
    private final AttendanceRepository attendanceRepository;
    private final StaffAttendanceRepository staffAttendanceRepository;
    private final PaymentRepository paymentRepository;
    private final ExpenseRepository expenseRepository;
    private final IncomeRepository incomeRepository;
    private final FeeDueRepository feeDueRepository;
    private final FeeStructureItemRepository feeStructureItemRepository;
    private final AuthContextService authContextService;

    public DashboardService(
            StudentRepository studentRepository,
            StaffRepository staffRepository,
            AttendanceRepository attendanceRepository,
            StaffAttendanceRepository staffAttendanceRepository,
            PaymentRepository paymentRepository,
            ExpenseRepository expenseRepository,
            IncomeRepository incomeRepository,
            FeeDueRepository feeDueRepository,
            FeeStructureItemRepository feeStructureItemRepository,
            AuthContextService authContextService
    ) {
        this.studentRepository = studentRepository;
        this.staffRepository = staffRepository;
        this.attendanceRepository = attendanceRepository;
        this.staffAttendanceRepository = staffAttendanceRepository;
        this.paymentRepository = paymentRepository;
        this.expenseRepository = expenseRepository;
        this.incomeRepository = incomeRepository;
        this.feeDueRepository = feeDueRepository;
        this.feeStructureItemRepository = feeStructureItemRepository;
        this.authContextService = authContextService;
    }

    public DashboardKpiResponse getKpis(Long schoolId) {
        Long effectiveSchoolId = authContextService.resolveSchoolId(schoolId);
        long studentTotal = studentRepository.countBySchoolId(effectiveSchoolId);
        long staffTotal = staffRepository.countBySchoolId(effectiveSchoolId);
        LocalDate today = LocalDate.now();
        int currentMonth = today.getMonthValue();
        int currentYear = today.getYear();

        long studentPresent = attendanceRepository.countBySchoolIdAndAttendanceDateAndStatus(
                effectiveSchoolId, today, "PRESENT"
        );
        long staffPresent = staffAttendanceRepository.countBySchoolIdAndAttendanceDateAndStatus(
                effectiveSchoolId, today, "PRESENT"
        );

        BigDecimal monthlyCollection = paymentRepository.sumAmountBySchoolAndMonthAndYear(
                effectiveSchoolId, currentMonth, currentYear
        );
        if (monthlyCollection == null) monthlyCollection = BigDecimal.ZERO;

        BigDecimal monthlyExpense = expenseRepository.sumAmountBySchoolAndMonthAndYear(
                effectiveSchoolId, currentMonth, currentYear
        );
        if (monthlyExpense == null) monthlyExpense = BigDecimal.ZERO;

        BigDecimal monthlyIncome = incomeRepository.sumAmountBySchoolAndMonthAndYear(
                effectiveSchoolId, currentMonth, currentYear
        );
        if (monthlyIncome == null) monthlyIncome = BigDecimal.ZERO;

        long newAdmissions = studentRepository.countNewAdmissionsBySchoolAndMonthAndYear(
                effectiveSchoolId, currentMonth, currentYear
        );

        // Calculate collection trend comparing to previous month
        LocalDate lastMonthDate = today.minusMonths(1);
        BigDecimal lastMonthCollection = paymentRepository.sumAmountBySchoolAndMonthAndYear(
                effectiveSchoolId, lastMonthDate.getMonthValue(), lastMonthDate.getYear()
        );
        String collectionTrend = "+0%";
        if (lastMonthCollection != null && lastMonthCollection.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal diff = monthlyCollection.subtract(lastMonthCollection);
            BigDecimal percentage = diff.multiply(BigDecimal.valueOf(100))
                    .divide(lastMonthCollection, 1, java.math.RoundingMode.HALF_UP);
            collectionTrend = (percentage.compareTo(BigDecimal.ZERO) >= 0 ? "+" : "") + percentage.toPlainString() + "%";
        } else if (monthlyCollection.compareTo(BigDecimal.ZERO) > 0) {
            collectionTrend = "+100%";
        }

        return new DashboardKpiResponse(
                monthlyCollection,
                monthlyExpense,
                monthlyExpense.multiply(BigDecimal.valueOf(1.05)).setScale(2, java.math.RoundingMode.HALF_UP),
                newAdmissions,
                staffPresent,
                staffTotal,
                studentPresent,
                studentTotal,
                collectionTrend,
                monthlyIncome
        );
    }

    public List<CollectionExpensePointResponse> getCollectionVsExpense(Long schoolId, int year) {
        Long effectiveSchoolId = authContextService.resolveSchoolId(schoolId);
        List<Object[]> monthlyCollections = paymentRepository.sumMonthlyAmountBySchoolAndYear(effectiveSchoolId, year);
        java.util.Map<Integer, BigDecimal> collectionMap = new java.util.HashMap<>();
        if (monthlyCollections != null) {
            for (Object[] row : monthlyCollections) {
                if (row != null && row.length >= 2 && row[0] != null && row[1] != null) {
                    Integer month = ((Number) row[0]).intValue();
                    BigDecimal amount = (BigDecimal) row[1];
                    collectionMap.put(month, amount);
                }
            }
        }

        List<Object[]> monthlyIncomes = incomeRepository.sumMonthlyAmountBySchoolAndYear(effectiveSchoolId, year);
        if (monthlyIncomes != null) {
            for (Object[] row : monthlyIncomes) {
                if (row != null && row.length >= 2 && row[0] != null && row[1] != null) {
                    Integer month = ((Number) row[0]).intValue();
                    BigDecimal amount = (BigDecimal) row[1];
                    collectionMap.merge(month, amount, BigDecimal::add);
                }
            }
        }

        List<Object[]> monthlyExpenses = expenseRepository.sumMonthlyAmountBySchoolAndYear(effectiveSchoolId, year);
        java.util.Map<Integer, BigDecimal> expenseMap = new java.util.HashMap<>();
        if (monthlyExpenses != null) {
            for (Object[] row : monthlyExpenses) {
                if (row != null && row.length >= 2 && row[0] != null && row[1] != null) {
                    Integer month = ((Number) row[0]).intValue();
                    BigDecimal amount = (BigDecimal) row[1];
                    expenseMap.put(month, amount);
                }
            }
        }

        List<CollectionExpensePointResponse> result = new ArrayList<>();
        for (int month = 1; month <= 12; month++) {
            BigDecimal col = collectionMap.getOrDefault(month, BigDecimal.ZERO);
            BigDecimal exp = expenseMap.getOrDefault(month, BigDecimal.ZERO);
            result.add(new CollectionExpensePointResponse(
                    Month.of(month).name().substring(0, 3),
                    col,
                    exp
            ));
        }
        return result;
    }

    public List<DashboardBreakdownResponse.EarningModeItem> getEarningModes(Long schoolId) {
        Long effectiveSchoolId = authContextService.resolveSchoolId(schoolId);
        List<Object[]> rows = paymentRepository.sumAmountByPaymentMode(effectiveSchoolId);
        BigDecimal total = BigDecimal.ZERO;
        BigDecimal onlineTotal = BigDecimal.ZERO;
        BigDecimal cashTotal = BigDecimal.ZERO;

        if (rows != null) {
            for (Object[] row : rows) {
                if (row != null && row.length >= 2 && row[0] != null && row[1] != null) {
                    String mode = row[0].toString().toUpperCase();
                    BigDecimal amt = (BigDecimal) row[1];
                    total = total.add(amt);
                    if (mode.contains("ONLINE") || mode.contains("UPI") || mode.contains("CARD") || mode.contains("NET") || mode.contains("GATEWAY")) {
                        onlineTotal = onlineTotal.add(amt);
                    } else {
                        cashTotal = cashTotal.add(amt);
                    }
                }
            }
        }

        List<DashboardBreakdownResponse.EarningModeItem> items = new ArrayList<>();
        if (total.compareTo(BigDecimal.ZERO) > 0) {
            double onlinePct = onlineTotal.multiply(BigDecimal.valueOf(100))
                    .divide(total, 1, java.math.RoundingMode.HALF_UP).doubleValue();
            double cashPct = Math.max(0, 100.0 - onlinePct);
            items.add(new DashboardBreakdownResponse.EarningModeItem("Online", onlineTotal, onlinePct));
            items.add(new DashboardBreakdownResponse.EarningModeItem("Cash/Other", cashTotal, cashPct));
        } else {
            items.add(new DashboardBreakdownResponse.EarningModeItem("Online", BigDecimal.ZERO, 0.0));
            items.add(new DashboardBreakdownResponse.EarningModeItem("Cash/Other", BigDecimal.ZERO, 0.0));
        }
        return items;
    }

    public List<DashboardBreakdownResponse.FeeDistributionItem> getFeeDistribution(Long schoolId) {
        Long effectiveSchoolId = authContextService.resolveSchoolId(schoolId);
        List<Object[]> rows = feeStructureItemRepository.sumAmountsByCategoryAndSchoolId(effectiveSchoolId);
        if (rows == null || rows.isEmpty()) {
            rows = feeDueRepository.sumFeesByCategory(effectiveSchoolId);
        }
        BigDecimal total = BigDecimal.ZERO;
        java.util.Map<String, BigDecimal> catMap = new java.util.LinkedHashMap<>();

        if (rows != null) {
            for (Object[] row : rows) {
                if (row != null && row.length >= 2 && row[0] != null && row[1] != null) {
                    String cat = row[0].toString();
                    BigDecimal amt = (BigDecimal) row[1];
                    total = total.add(amt);
                    catMap.put(cat, amt);
                }
            }
        }

        List<DashboardBreakdownResponse.FeeDistributionItem> items = new ArrayList<>();
        if (total.compareTo(BigDecimal.ZERO) > 0) {
            for (var entry : catMap.entrySet()) {
                double pct = entry.getValue().multiply(BigDecimal.valueOf(100))
                        .divide(total, 1, java.math.RoundingMode.HALF_UP).doubleValue();
                items.add(new DashboardBreakdownResponse.FeeDistributionItem(entry.getKey(), entry.getValue(), pct));
            }
        }
        return items;
    }

    public List<DashboardBreakdownResponse.ClassDistributionItem> getClassDistribution(Long schoolId) {
        Long effectiveSchoolId = authContextService.resolveSchoolId(schoolId);
        List<Object[]> rows = studentRepository.countStudentsPerClass(effectiveSchoolId);
        long totalStudents = studentRepository.countBySchoolId(effectiveSchoolId);

        List<DashboardBreakdownResponse.ClassDistributionItem> items = new ArrayList<>();
        if (rows != null) {
            for (Object[] row : rows) {
                if (row != null && row.length >= 3 && row[0] != null) {
                    Long classId = ((Number) row[0]).longValue();
                    String className = row[1] != null ? row[1].toString() : "Class " + classId;
                    long count = row[2] != null ? ((Number) row[2]).longValue() : 0L;
                    double pct = totalStudents > 0
                            ? Math.round((double) count * 1000.0 / (double) totalStudents) / 10.0
                            : 0.0;
                    items.add(new DashboardBreakdownResponse.ClassDistributionItem(classId, className, count, pct));
                }
            }
        }
        return items;
    }

    public List<DashboardBreakdownResponse.StudentBirthdayItem> getStudentBirthdays(Long schoolId) {
        Long effectiveSchoolId = authContextService.resolveSchoolId(schoolId);
        LocalDate today = LocalDate.now();
        List<Student> students = studentRepository.findStudentsWithBirthdayToday(
                effectiveSchoolId, today.getMonthValue(), today.getDayOfMonth()
        );

        List<DashboardBreakdownResponse.StudentBirthdayItem> items = new ArrayList<>();
        if (students != null) {
            for (Student s : students) {
                String name = (s.getFirstName() != null ? s.getFirstName() + " " : "") +
                        (s.getLastName() != null ? s.getLastName() : "");
                if (name.isBlank() && s.getName() != null) name = s.getName();
                String className = s.getSchoolClass() != null ? s.getSchoolClass().getName() : "";
                String dob = s.getDateOfBirth() != null ? s.getDateOfBirth().toString() : "";
                items.add(new DashboardBreakdownResponse.StudentBirthdayItem(
                        s.getId(), name.trim(), className, dob
                ));
            }
        }
        return items;
    }

    public List<ActivityFeedResponse> getActivityFeed(int size) {
        List<ActivityFeedResponse> activities = new ArrayList<>();
        activities.add(new ActivityFeedResponse(
                UUID.randomUUID().toString(),
                "ATTENDANCE_SYNC",
                "Attendance Sync",
                "Attendance data was synced successfully.",
                LocalDateTime.now(),
                "INFO"
        ));
        return activities.stream().limit(size).toList();
    }
}
