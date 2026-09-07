package com.school.erp.service.hr;

import com.school.erp.dto.hr.StaffLeaveDTO;
import com.school.erp.dto.hr.StaffLeaveRequest;
import com.school.erp.entity.Staff;
import com.school.erp.entity.hr.SchoolLeaveType;
import com.school.erp.entity.hr.StaffLeave;
import com.school.erp.entity.hr.StaffLeaveBalance;
import com.school.erp.repository.StaffRepository;
import com.school.erp.repository.hr.SchoolLeaveTypeRepository;
import com.school.erp.repository.hr.StaffLeaveBalanceRepository;
import com.school.erp.repository.hr.StaffLeaveRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StaffLeaveService {

    private final StaffLeaveRepository staffLeaveRepository;
    private final StaffRepository staffRepository;
    private final SchoolLeaveTypeRepository leaveTypeRepository;
    private final StaffLeaveBalanceRepository leaveBalanceRepository;

    @Transactional
    public StaffLeaveDTO applyLeave(Long schoolId, StaffLeaveRequest request) {
        Staff staff = staffRepository.findByIdAndSchoolId(request.getStaffId(), schoolId)
                .orElseThrow(() -> new RuntimeException("Staff not found"));

        SchoolLeaveType leaveType = leaveTypeRepository.findById(request.getLeaveTypeId())
                .orElseThrow(() -> new RuntimeException("Leave type not found"));

        if (!leaveType.getSchool().getId().equals(schoolId)) {
            throw new RuntimeException("Leave type doesn't belong to this school");
        }

        long days = ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate()) + 1;
        if (days <= 0) {
            throw new RuntimeException("End date must be after start date");
        }

        StaffLeave leave = new StaffLeave();
        leave.setSchool(staff.getSchool());
        leave.setStaff(staff);
        leave.setLeaveTypeId(leaveType.getId());
        leave.setStartDate(request.getStartDate());
        leave.setEndDate(request.getEndDate());
        leave.setNumberOfDays(BigDecimal.valueOf(days));
        leave.setReason(request.getReason());
        leave.setStatus("PENDING");

        return mapToDTO(staffLeaveRepository.save(leave));
    }

    @Transactional
    public StaffLeaveDTO approveLeave(Long schoolId, Long leaveId, Long adminStaffId, String status) {
        StaffLeave leave = staffLeaveRepository.findById(leaveId)
                .orElseThrow(() -> new RuntimeException("Leave not found"));

        if (!leave.getSchool().getId().equals(schoolId)) {
            throw new RuntimeException("Leave doesn't belong to this school");
        }
        
        Staff admin = staffRepository.findById(adminStaffId)
                .orElseThrow(() -> new RuntimeException("Admin staff not found"));

        leave.setStatus(status);
        leave.setApprovedBy(admin.getId());

        // Update Leave Balance if APPROVED
        if ("APPROVED".equalsIgnoreCase(status)) {
            StaffLeaveBalance balance = leaveBalanceRepository
                    .findByStaffIdAndLeaveTypeId(leave.getStaff().getId(), leave.getLeaveTypeId())
                    .orElseGet(() -> {
                        StaffLeaveBalance b = new StaffLeaveBalance();
                        b.setSchool(leave.getSchool());
                        b.setStaff(leave.getStaff());
                        b.setLeaveTypeId(leave.getLeaveTypeId());
                        b.setTotalLeaves(BigDecimal.ZERO);
                        b.setUsedLeaves(BigDecimal.ZERO);
                        b.setRemainingLeaves(BigDecimal.ZERO);
                        return b;
                    });
            
            balance.setUsedLeaves(balance.getUsedLeaves().add(leave.getNumberOfDays()));
            balance.setRemainingLeaves(balance.getTotalLeaves().subtract(balance.getUsedLeaves()));
            leaveBalanceRepository.save(balance);
        }

        return mapToDTO(staffLeaveRepository.save(leave));
    }

    public List<StaffLeaveDTO> getAllLeaves(Long schoolId) {
        return staffLeaveRepository.findBySchoolId(schoolId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private StaffLeaveDTO mapToDTO(StaffLeave leave) {
        StaffLeaveDTO dto = new StaffLeaveDTO();
        dto.setId(leave.getId());
        dto.setSchoolId(leave.getSchool().getId());
        dto.setStaffId(leave.getStaff().getId());
        dto.setStaffName(leave.getStaff().getFirstName() + " " + leave.getStaff().getLastName());
        dto.setLeaveTypeId(leave.getLeaveTypeId());
        dto.setLeaveTypeName("Leave Type " + leave.getLeaveTypeId()); // Simple workaround since leaveTypeName is not on entity
        dto.setStartDate(leave.getStartDate());
        dto.setEndDate(leave.getEndDate());
        dto.setNumberOfDays(leave.getNumberOfDays());
        dto.setReason(leave.getReason());
        dto.setStatus(leave.getStatus());
        dto.setApprovedBy(leave.getApprovedBy());
        return dto;
    }
}
