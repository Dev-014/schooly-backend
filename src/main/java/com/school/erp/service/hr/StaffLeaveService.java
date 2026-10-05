package com.school.erp.service.hr;

import com.school.erp.dto.hr.SchoolLeaveTypeDTO;
import com.school.erp.dto.hr.SchoolLeaveTypeRequest;
import com.school.erp.dto.hr.StaffLeaveBalanceDTO;
import com.school.erp.dto.hr.StaffLeaveBalanceUpdateRequest;
import com.school.erp.dto.hr.StaffLeaveDTO;
import com.school.erp.dto.hr.StaffLeaveRequest;
import com.school.erp.entity.hr.Staff;
import com.school.erp.entity.hr.SchoolLeaveType;
import com.school.erp.entity.hr.StaffLeave;
import com.school.erp.entity.hr.StaffLeaveBalance;
import com.school.erp.entity.superadmin.School;
import com.school.erp.repository.hr.StaffRepository;
import com.school.erp.repository.hr.SchoolLeaveTypeRepository;
import com.school.erp.repository.hr.StaffLeaveBalanceRepository;
import com.school.erp.repository.hr.StaffLeaveRepository;
import com.school.erp.repository.superadmin.SchoolRepository;
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
    private final SchoolRepository schoolRepository;

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
        dto.setStaffName((leave.getStaff().getFirstName() != null ? leave.getStaff().getFirstName() : "") + " " +
                (leave.getStaff().getLastName() != null ? leave.getStaff().getLastName() : ""));
        dto.setLeaveTypeId(leave.getLeaveTypeId());
        String typeName = leaveTypeRepository.findById(leave.getLeaveTypeId())
                .map(SchoolLeaveType::getName)
                .orElse("Leave Type " + leave.getLeaveTypeId());
        dto.setLeaveTypeName(typeName);
        dto.setStartDate(leave.getStartDate());
        dto.setEndDate(leave.getEndDate());
        dto.setNumberOfDays(leave.getNumberOfDays());
        dto.setReason(leave.getReason());
        dto.setStatus(leave.getStatus());
        dto.setApprovedBy(leave.getApprovedBy());
        return dto;
    }

    // Leave Types CRUD
    @Transactional(readOnly = true)
    public List<SchoolLeaveTypeDTO> getLeaveTypes(Long schoolId) {
        return leaveTypeRepository.findBySchoolId(schoolId).stream()
                .map(this::mapLeaveTypeToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public SchoolLeaveTypeDTO createLeaveType(Long schoolId, SchoolLeaveTypeRequest request) {
        School school = schoolRepository.findById(schoolId)
                .orElseThrow(() -> new RuntimeException("School not found"));

        SchoolLeaveType leaveType = new SchoolLeaveType();
        leaveType.setSchool(school);
        leaveType.setName(request.getName());
        leaveType.setDaysAllowed(request.getDaysAllowed() != null ? request.getDaysAllowed() : 0);
        leaveType.setIsPaid(request.getIsPaid() != null ? request.getIsPaid() : true);
        leaveType.setApplicableRoles(request.getApplicableRoles());

        return mapLeaveTypeToDTO(leaveTypeRepository.save(leaveType));
    }

    @Transactional
    public SchoolLeaveTypeDTO updateLeaveType(Long schoolId, Long id, SchoolLeaveTypeRequest request) {
        SchoolLeaveType leaveType = leaveTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Leave type not found"));

        if (!leaveType.getSchool().getId().equals(schoolId)) {
            throw new RuntimeException("Leave type doesn't belong to this school");
        }

        leaveType.setName(request.getName());
        if (request.getDaysAllowed() != null) {
            leaveType.setDaysAllowed(request.getDaysAllowed());
        }
        if (request.getIsPaid() != null) {
            leaveType.setIsPaid(request.getIsPaid());
        }
        leaveType.setApplicableRoles(request.getApplicableRoles());

        return mapLeaveTypeToDTO(leaveTypeRepository.save(leaveType));
    }

    @Transactional
    public void deleteLeaveType(Long schoolId, Long id) {
        SchoolLeaveType leaveType = leaveTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Leave type not found"));

        if (!leaveType.getSchool().getId().equals(schoolId)) {
            throw new RuntimeException("Leave type doesn't belong to this school");
        }

        leaveTypeRepository.delete(leaveType);
    }

    private SchoolLeaveTypeDTO mapLeaveTypeToDTO(SchoolLeaveType lt) {
        return SchoolLeaveTypeDTO.builder()
                .id(lt.getId())
                .schoolId(lt.getSchool().getId())
                .name(lt.getName())
                .daysAllowed(lt.getDaysAllowed())
                .isPaid(lt.getIsPaid())
                .applicableRoles(lt.getApplicableRoles())
                .build();
    }

    // Leave Balances
    @Transactional(readOnly = true)
    public List<StaffLeaveBalanceDTO> getStaffLeaveBalances(Long schoolId, Long staffId) {
        Staff staff = staffRepository.findByIdAndSchoolId(staffId, schoolId)
                .orElseThrow(() -> new RuntimeException("Staff not found in this school"));

        List<SchoolLeaveType> leaveTypes = leaveTypeRepository.findBySchoolId(schoolId);
        List<StaffLeaveBalance> existingBalances = leaveBalanceRepository.findByStaffId(staffId);
        java.util.Map<Long, StaffLeaveBalance> balanceMap = existingBalances.stream()
                .collect(Collectors.toMap(StaffLeaveBalance::getLeaveTypeId, b -> b, (b1, b2) -> b1));

        String staffName = ((staff.getFirstName() != null ? staff.getFirstName() : "") + " " +
                (staff.getLastName() != null ? staff.getLastName() : "")).trim();

        return leaveTypes.stream().map(lt -> {
            StaffLeaveBalance bal = balanceMap.get(lt.getId());
            BigDecimal total = bal != null && bal.getTotalLeaves() != null
                    ? bal.getTotalLeaves()
                    : BigDecimal.valueOf(lt.getDaysAllowed() != null ? lt.getDaysAllowed() : 0);
            BigDecimal used = bal != null && bal.getUsedLeaves() != null
                    ? bal.getUsedLeaves()
                    : BigDecimal.ZERO;
            BigDecimal remaining = bal != null && bal.getRemainingLeaves() != null
                    ? bal.getRemainingLeaves()
                    : total.subtract(used);

            return StaffLeaveBalanceDTO.builder()
                    .id(bal != null ? bal.getId() : null)
                    .schoolId(schoolId)
                    .staffId(staff.getId())
                    .staffName(staffName)
                    .leaveTypeId(lt.getId())
                    .leaveTypeName(lt.getName())
                    .totalLeaves(total)
                    .usedLeaves(used)
                    .remainingLeaves(remaining)
                    .build();
        }).collect(Collectors.toList());
    }

    @Transactional
    public StaffLeaveBalanceDTO updateStaffLeaveBalance(Long schoolId, Long staffId, StaffLeaveBalanceUpdateRequest request) {
        Staff staff = staffRepository.findByIdAndSchoolId(staffId, schoolId)
                .orElseThrow(() -> new RuntimeException("Staff not found in this school"));

        SchoolLeaveType leaveType = leaveTypeRepository.findById(request.getLeaveTypeId())
                .orElseThrow(() -> new RuntimeException("Leave type not found"));

        if (!leaveType.getSchool().getId().equals(schoolId)) {
            throw new RuntimeException("Leave type does not belong to this school");
        }

        StaffLeaveBalance balance = leaveBalanceRepository
                .findByStaffIdAndLeaveTypeId(staffId, request.getLeaveTypeId())
                .orElseGet(() -> {
                    StaffLeaveBalance newBal = new StaffLeaveBalance();
                    newBal.setSchool(staff.getSchool());
                    newBal.setStaff(staff);
                    newBal.setLeaveTypeId(leaveType.getId());
                    newBal.setUsedLeaves(BigDecimal.ZERO);
                    return newBal;
                });

        balance.setTotalLeaves(request.getTotalLeaves());
        if (request.getUsedLeaves() != null) {
            balance.setUsedLeaves(request.getUsedLeaves());
        }
        if (request.getRemainingLeaves() != null) {
            balance.setRemainingLeaves(request.getRemainingLeaves());
        } else {
            balance.setRemainingLeaves(balance.getTotalLeaves().subtract(balance.getUsedLeaves()));
        }

        StaffLeaveBalance saved = leaveBalanceRepository.save(balance);
        String staffName = ((staff.getFirstName() != null ? staff.getFirstName() : "") + " " +
                (staff.getLastName() != null ? staff.getLastName() : "")).trim();

        return StaffLeaveBalanceDTO.builder()
                .id(saved.getId())
                .schoolId(schoolId)
                .staffId(staff.getId())
                .staffName(staffName)
                .leaveTypeId(leaveType.getId())
                .leaveTypeName(leaveType.getName())
                .totalLeaves(saved.getTotalLeaves())
                .usedLeaves(saved.getUsedLeaves())
                .remainingLeaves(saved.getRemainingLeaves())
                .build();
    }
}
