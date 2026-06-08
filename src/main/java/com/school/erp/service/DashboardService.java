package com.school.erp.service;

import com.school.erp.dto.dashboard.ActivityFeedResponse;
import com.school.erp.dto.dashboard.CollectionExpensePointResponse;
import com.school.erp.dto.dashboard.DashboardKpiResponse;
import com.school.erp.repository.AttendanceRepository;
import com.school.erp.repository.PaymentRepository;
import com.school.erp.repository.StaffRepository;
import com.school.erp.repository.StudentRepository;
import com.school.erp.security.AuthContextService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
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
        List<CollectionExpensePointResponse> result = new ArrayList<>();
        for (int month = 1; month <= 12; month++) {
            BigDecimal monthlyCollection = paymentRepository.sumAmountBySchoolAndMonthAndYear(effectiveSchoolId, month, year);
            result.add(new CollectionExpensePointResponse(
                    Month.of(month).name().substring(0, 3),
                    monthlyCollection == null ? BigDecimal.ZERO : monthlyCollection,
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
