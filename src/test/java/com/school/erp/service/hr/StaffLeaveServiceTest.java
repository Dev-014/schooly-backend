package com.school.erp.service.hr;

import com.school.erp.dto.hr.StaffLeaveDTO;
import com.school.erp.dto.hr.StaffLeaveRequest;
import com.school.erp.entity.hr.SchoolLeaveType;
import com.school.erp.entity.hr.Staff;
import com.school.erp.entity.hr.StaffLeave;
import com.school.erp.entity.hr.StaffLeaveBalance;
import com.school.erp.entity.superadmin.School;
import com.school.erp.repository.hr.SchoolLeaveTypeRepository;
import com.school.erp.repository.hr.StaffLeaveBalanceRepository;
import com.school.erp.repository.hr.StaffLeaveRepository;
import com.school.erp.repository.hr.StaffRepository;
import com.school.erp.repository.superadmin.SchoolRepository;
import com.school.erp.security.AuthContextHolder;
import com.school.erp.security.AuthenticatedUser;
import com.school.erp.entity.auth.UserRole;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StaffLeaveServiceTest {

    @Mock
    private StaffLeaveRepository staffLeaveRepository;

    @Mock
    private StaffRepository staffRepository;

    @Mock
    private SchoolLeaveTypeRepository leaveTypeRepository;

    @Mock
    private StaffLeaveBalanceRepository leaveBalanceRepository;

    @Mock
    private SchoolRepository schoolRepository;

    @InjectMocks
    private StaffLeaveService leaveService;

    private School school;
    private Staff staff;
    private SchoolLeaveType leaveType;

    @BeforeEach
    void setUp() {
        school = new School();
        school.setId(1000L);
        school.setName("Test School");

        staff = new Staff();
        staff.setId(2L);
        staff.setUserId(20L);
        staff.setSchool(school);
        staff.setFirstName("John");
        staff.setLastName("Doe");

        leaveType = new SchoolLeaveType();
        leaveType.setId(1L);
        leaveType.setName("Casual Leave");
        leaveType.setSchool(school);
        leaveType.setDaysAllowed(12);
        leaveType.setIsPaid(true);
    }

    @AfterEach
    void tearDown() {
        AuthContextHolder.clear();
    }

    @Test
    void testApplyLeave_SuccessWithExplicitStaffId() {
        StaffLeaveRequest request = new StaffLeaveRequest();
        request.setStaffId(2L);
        request.setLeaveTypeId(1L);
        request.setStartDate(LocalDate.of(2026, 10, 27));
        request.setEndDate(LocalDate.of(2026, 10, 29));
        request.setReason("Family event");

        when(staffRepository.findByIdAndSchoolId(2L, 1000L)).thenReturn(Optional.of(staff));
        when(leaveTypeRepository.findById(1L)).thenReturn(Optional.of(leaveType));
        when(staffLeaveRepository.save(any(StaffLeave.class))).thenAnswer(invocation -> {
            StaffLeave sl = invocation.getArgument(0);
            sl.setId(101L);
            return sl;
        });

        StaffLeaveDTO result = leaveService.applyLeave(1000L, request);

        assertNotNull(result);
        assertEquals(101L, result.getId());
        assertEquals(2L, result.getStaffId());
        assertEquals("John Doe", result.getStaffName());
        assertEquals(BigDecimal.valueOf(3), result.getNumberOfDays());
        assertEquals("PENDING", result.getStatus());
        assertEquals(LocalDate.of(2026, 10, 27), result.getFromDate());
        assertEquals(LocalDate.of(2026, 10, 29), result.getToDate());
    }

    @Test
    void testApplyLeave_ResolvesStaffFromAuthContext_WhenStaffIdNull() {
        AuthContextHolder.set(new AuthenticatedUser(20L, 1000L, UserRole.TEACHER));

        StaffLeaveRequest request = new StaffLeaveRequest();
        request.setLeaveTypeId(1L);
        request.setStartDate(LocalDate.of(2026, 10, 27));
        request.setEndDate(LocalDate.of(2026, 10, 29));
        request.setReason("Sick leave");

        when(staffRepository.findBySchoolIdAndUserId(1000L, 20L)).thenReturn(Optional.of(staff));
        when(leaveTypeRepository.findById(1L)).thenReturn(Optional.of(leaveType));
        when(staffLeaveRepository.save(any(StaffLeave.class))).thenAnswer(invocation -> {
            StaffLeave sl = invocation.getArgument(0);
            sl.setId(102L);
            return sl;
        });

        StaffLeaveDTO result = leaveService.applyLeave(1000L, request);

        assertNotNull(result);
        assertEquals(2L, result.getStaffId());
        assertEquals("John Doe", result.getStaffName());
    }

    @Test
    void testApplyLeave_ResolvesStaff_WhenUserIdPassedAsStaffId() {
        // When client sends userId (20L) instead of staff primary key (2L)
        StaffLeaveRequest request = new StaffLeaveRequest();
        request.setStaffId(20L);
        request.setLeaveTypeId(1L);
        request.setStartDate(LocalDate.of(2026, 10, 27));
        request.setEndDate(LocalDate.of(2026, 10, 29));
        request.setReason("Medical appointment");

        when(staffRepository.findByIdAndSchoolId(20L, 1000L)).thenReturn(Optional.empty());
        when(staffRepository.findBySchoolIdAndUserId(1000L, 20L)).thenReturn(Optional.of(staff));
        when(leaveTypeRepository.findById(1L)).thenReturn(Optional.of(leaveType));
        when(staffLeaveRepository.save(any(StaffLeave.class))).thenAnswer(invocation -> {
            StaffLeave sl = invocation.getArgument(0);
            sl.setId(103L);
            return sl;
        });

        StaffLeaveDTO result = leaveService.applyLeave(1000L, request);

        assertNotNull(result);
        assertEquals(2L, result.getStaffId());
        assertEquals("John Doe", result.getStaffName());
    }

    @Test
    void testApplyLeave_ThrowsWhenEndDateBeforeStartDate() {
        StaffLeaveRequest request = new StaffLeaveRequest();
        request.setStaffId(2L);
        request.setLeaveTypeId(1L);
        request.setStartDate(LocalDate.of(2026, 10, 29));
        request.setEndDate(LocalDate.of(2026, 10, 27));

        when(staffRepository.findByIdAndSchoolId(2L, 1000L)).thenReturn(Optional.of(staff));
        when(leaveTypeRepository.findById(1L)).thenReturn(Optional.of(leaveType));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> leaveService.applyLeave(1000L, request));
        assertTrue(ex.getMessage().contains("End date must be after start date"));
    }

    @Test
    void testGetAllLeaves_SuccessAndMapsWithoutLazyLoadingException() {
        StaffLeave leave = new StaffLeave();
        leave.setId(50L);
        leave.setSchool(school);
        leave.setStaff(staff);
        leave.setLeaveTypeId(1L);
        leave.setStartDate(LocalDate.of(2026, 10, 27));
        leave.setEndDate(LocalDate.of(2026, 10, 28));
        leave.setNumberOfDays(BigDecimal.valueOf(2));
        leave.setStatus("PENDING");

        when(leaveTypeRepository.findBySchoolId(1000L)).thenReturn(List.of(leaveType));
        when(staffLeaveRepository.findBySchoolId(1000L)).thenReturn(List.of(leave));

        List<StaffLeaveDTO> leaves = leaveService.getAllLeaves(1000L);

        assertEquals(1, leaves.size());
        StaffLeaveDTO dto = leaves.get(0);
        assertEquals(50L, dto.getId());
        assertEquals("John Doe", dto.getStaffName());
        assertEquals("Casual Leave", dto.getLeaveTypeName());
        assertEquals(LocalDate.of(2026, 10, 27), dto.getStartDate());
        assertEquals(LocalDate.of(2026, 10, 27), dto.getFromDate());
        assertEquals(LocalDate.of(2026, 10, 28), dto.getToDate());
    }

    @Test
    void testApproveLeave_UpdatesBalanceWhenApproved() {
        StaffLeave leave = new StaffLeave();
        leave.setId(50L);
        leave.setSchool(school);
        leave.setStaff(staff);
        leave.setLeaveTypeId(1L);
        leave.setNumberOfDays(BigDecimal.valueOf(3));
        leave.setStatus("PENDING");

        StaffLeaveBalance balance = new StaffLeaveBalance();
        balance.setId(1L);
        balance.setSchool(school);
        balance.setStaff(staff);
        balance.setLeaveTypeId(1L);
        balance.setTotalLeaves(BigDecimal.valueOf(12));
        balance.setUsedLeaves(BigDecimal.ZERO);
        balance.setRemainingLeaves(BigDecimal.valueOf(12));

        when(staffLeaveRepository.findById(50L)).thenReturn(Optional.of(leave));
        when(leaveBalanceRepository.findByStaffIdAndLeaveTypeId(2L, 1L)).thenReturn(Optional.of(balance));
        when(staffLeaveRepository.save(any(StaffLeave.class))).thenReturn(leave);
        when(leaveTypeRepository.findById(1L)).thenReturn(Optional.of(leaveType));

        StaffLeaveDTO result = leaveService.approveLeave(1000L, 50L, 99L, "APPROVED");

        assertNotNull(result);
        assertEquals("APPROVED", result.getStatus());
        assertEquals(BigDecimal.valueOf(3), balance.getUsedLeaves());
        assertEquals(BigDecimal.valueOf(9), balance.getRemainingLeaves());
    }


    @Test
    void testApproveLeave_ThrowsWhenSelfApprovalAttempted() {
        AuthContextHolder.set(new AuthenticatedUser(1L, 1000L, UserRole.ADMIN));

        StaffLeave leave = new StaffLeave();
        leave.setId(50L);
        leave.setSchool(school);
        leave.setStaff(staff); // staff id is 2L

        when(staffLeaveRepository.findById(50L)).thenReturn(Optional.of(leave));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> leaveService.approveLeave(1000L, 50L, 2L, "APPROVED"));
        assertTrue(ex.getMessage().contains("Staff cannot approve their own leave request"));
    }
}
