package com.school.erp.service.common;

import com.school.erp.dto.dashboard.ActivityFeedResponse;
import com.school.erp.dto.dashboard.CollectionExpensePointResponse;
import com.school.erp.dto.dashboard.DashboardKpiResponse;
import com.school.erp.repository.attendance.AttendanceRepository;
import com.school.erp.repository.finance.PaymentRepository;
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
    private final PaymentRepository paymentRepository;
    private final AuthContextService authContextService;

    public DashboardService(
            StudentRepository studentRepository,
            StaffRepository staffRepository,
            AttendanceRepository attendanceRepository,
            PaymentRepository paymentRepository,
            AuthContextService authContextService
    ) {
        this.studentRepository = studentRepository;
        this.staffRepository = staffRepository;
        this.attendanceRepository = attendanceRepository;
        this.paymentRepository = paymentRepository;
        this.authContextService = authContextService;
    }

    public DashboardKpiResponse getKpis(Long schoolId) {
        Long effectiveSchoolId = authContextService.resolveSchoolId(schoolId);
        long studentTotal = studentRepository.countBySchoolId(effectiveSchoolId);
        long staffTotal = staffRepository.countBySchoolId(effectiveSchoolId);
        long studentPresent = attendanceRepository.countBySchoolIdAndAttendanceDateAndStatus(
                effectiveSchoolId, LocalDate.now(), "PRESENT"
        );

        BigDecimal monthlyCollection = paymentRepository.sumAmountBySchoolAndMonthAndYear(
                effectiveSchoolId, LocalDate.now().getMonthValue(), LocalDate.now().getYear()
        );

        return new DashboardKpiResponse(
                monthlyCollection == null ? BigDecimal.ZERO : monthlyCollection,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                0,
                0,
                staffTotal,
                studentPresent,
                studentTotal,
                "+0%"
        );
    }

    public List<CollectionExpensePointResponse> getCollectionVsExpense(Long schoolId, int year) {
        Long effectiveSchoolId = authContextService.resolveSchoolId(schoolId);
        List<Object[]> monthlyData = paymentRepository.sumMonthlyAmountBySchoolAndYear(effectiveSchoolId, year);
        java.util.Map<Integer, BigDecimal> monthMap = new java.util.HashMap<>();
        if (monthlyData != null) {
            for (Object[] row : monthlyData) {
                if (row != null && row.length >= 2 && row[0] != null && row[1] != null) {
                    Integer month = ((Number) row[0]).intValue();
                    BigDecimal amount = (BigDecimal) row[1];
                    monthMap.put(month, amount);
                }
            }
        }

        List<CollectionExpensePointResponse> result = new ArrayList<>();
        for (int month = 1; month <= 12; month++) {
            BigDecimal monthlyCollection = monthMap.getOrDefault(month, BigDecimal.ZERO);
            result.add(new CollectionExpensePointResponse(
                    Month.of(month).name().substring(0, 3),
                    monthlyCollection,
                    BigDecimal.ZERO
            ));
        }
        return result;
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
