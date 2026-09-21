package com.school.erp.service.hr;

import com.school.erp.dto.hr.BulkStaffAttendanceRequest;
import com.school.erp.dto.hr.StaffAttendanceDTO;
import com.school.erp.dto.hr.StaffAttendanceRequest;
import com.school.erp.dto.hr.StaffAttendanceStatsDTO;
import com.school.erp.entity.superadmin.School;
import com.school.erp.entity.hr.Staff;
import com.school.erp.entity.hr.StaffAttendance;
import com.school.erp.repository.hr.StaffRepository;
import com.school.erp.repository.hr.StaffAttendanceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StaffAttendanceServiceTest {

    @Mock
    private StaffAttendanceRepository staffAttendanceRepository;

    @Mock
    private StaffRepository staffRepository;

    @InjectMocks
    private StaffAttendanceService staffAttendanceService;

    private School school;
    private Staff staff1;
    private Staff staff2;
    private LocalDate today;

    @BeforeEach
    void setUp() {
        school = new School();
        school.setId(1L);
        school.setName("Greenwood High");

        staff1 = new Staff();
        staff1.setId(10L);
        staff1.setSchool(school);
        staff1.setFirstName("Arthur");
        staff1.setLastName("Morgan");
        staff1.setDepartment("History");
        staff1.setDesignation("Senior Teacher");
        staff1.setBiometricId("STF-0010");

        staff2 = new Staff();
        staff2.setId(20L);
        staff2.setSchool(school);
        staff2.setFirstName("Sadie");
        staff2.setLastName("Adler");
        staff2.setDepartment("Science");
        staff2.setDesignation("Lab Assistant");
        staff2.setBiometricId("STF-0020");

        today = LocalDate.of(2026, 9, 13);
    }

    @Test
    void testMarkAttendance_NewRecord_CalculatesHours() {
        StaffAttendanceRequest req = new StaffAttendanceRequest();
        req.setStaffId(10L);
        req.setAttendanceDate(today);
        req.setStatus("PRESENT");
        req.setCheckInTime(LocalTime.of(8, 0));
        req.setCheckOutTime(LocalTime.of(16, 30));
        req.setNotes("On time");

        when(staffRepository.findByIdAndSchoolId(10L, 1L)).thenReturn(Optional.of(staff1));
        when(staffAttendanceRepository.findByStaffIdAndAttendanceDate(10L, today)).thenReturn(Optional.empty());
        when(staffAttendanceRepository.save(any(StaffAttendance.class))).thenAnswer(invocation -> {
            StaffAttendance a = invocation.getArgument(0);
            a.setId(100L);
            return a;
        });

        StaffAttendanceDTO res = staffAttendanceService.markAttendance(1L, req);

        assertNotNull(res);
        assertEquals(100L, res.getId());
        assertEquals("PRESENT", res.getStatus());
        assertEquals(10L, res.getStaffId());
        assertEquals("Arthur Morgan", res.getStaffName());
        assertEquals(new BigDecimal("8.50"), res.getWorkingHours());
        verify(staffAttendanceRepository, times(1)).save(any(StaffAttendance.class));
    }

    @Test
    void testGetAttendanceRegister_ReturnsAllStaffWithAttendanceOrUnmarked() {
        when(staffRepository.findBySchoolId(1L)).thenReturn(List.of(staff1, staff2));

        StaffAttendance att1 = new StaffAttendance();
        att1.setId(101L);
        att1.setSchool(school);
        att1.setStaff(staff1);
        att1.setAttendanceDate(today);
        att1.setStatus("PRESENT");
        att1.setCheckInTime(LocalTime.of(8, 0));
        att1.setCheckOutTime(LocalTime.of(16, 0));
        att1.setWorkingHours(BigDecimal.valueOf(8.0));

        when(staffAttendanceRepository.findBySchoolIdAndAttendanceDate(1L, today)).thenReturn(List.of(att1));

        List<StaffAttendanceDTO> register = staffAttendanceService.getAttendanceRegister(1L, today, null, null);

        assertNotNull(register);
        assertEquals(2, register.size());

        StaffAttendanceDTO dto1 = register.stream().filter(r -> r.getStaffId().equals(10L)).findFirst().orElse(null);
        assertNotNull(dto1);
        assertEquals("PRESENT", dto1.getStatus());
        assertEquals(101L, dto1.getId());

        StaffAttendanceDTO dto2 = register.stream().filter(r -> r.getStaffId().equals(20L)).findFirst().orElse(null);
        assertNotNull(dto2);
        assertEquals("UNMARKED", dto2.getStatus());
        assertNull(dto2.getId());
    }

    @Test
    void testGetAttendanceStats_CalculatesPercentagesAndCounts() {
        when(staffRepository.findBySchoolId(1L)).thenReturn(List.of(staff1, staff2));

        StaffAttendance att1 = new StaffAttendance();
        att1.setSchool(school);
        att1.setStaff(staff1);
        att1.setAttendanceDate(today);
        att1.setStatus("PRESENT");

        when(staffAttendanceRepository.findBySchoolIdAndAttendanceDate(1L, today)).thenReturn(List.of(att1));

        StaffAttendanceStatsDTO stats = staffAttendanceService.getAttendanceStats(1L, today);

        assertNotNull(stats);
        assertEquals(2, stats.getTotalStaff());
        assertEquals(1, stats.getPresentCount());
        assertEquals(0, stats.getAbsentCount());
        assertEquals(1, stats.getUnmarkedCount());
        assertEquals(50.0, stats.getAttendancePercentage());
    }

    @Test
    void testBulkMarkAttendance_SavesMultipleStaffRecords() {
        StaffAttendanceRequest req1 = new StaffAttendanceRequest();
        req1.setStaffId(10L);
        req1.setAttendanceDate(today);
        req1.setStatus("PRESENT");

        StaffAttendanceRequest req2 = new StaffAttendanceRequest();
        req2.setStaffId(20L);
        req2.setAttendanceDate(today);
        req2.setStatus("LATE");

        BulkStaffAttendanceRequest bulkReq = BulkStaffAttendanceRequest.builder()
                .attendanceDate(today)
                .records(List.of(req1, req2))
                .build();

        when(staffRepository.findByIdAndSchoolId(10L, 1L)).thenReturn(Optional.of(staff1));
        when(staffRepository.findByIdAndSchoolId(20L, 1L)).thenReturn(Optional.of(staff2));
        when(staffAttendanceRepository.findByStaffIdAndAttendanceDate(anyLong(), any())).thenReturn(Optional.empty());
        when(staffAttendanceRepository.save(any(StaffAttendance.class))).thenAnswer(invocation -> invocation.getArgument(0));

        List<StaffAttendanceDTO> result = staffAttendanceService.bulkMarkAttendance(1L, bulkReq);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(staffAttendanceRepository, times(2)).save(any(StaffAttendance.class));
    }
}
