package com.school.erp.service.superadmin;

import com.school.erp.dto.ApplyLeaveRequest;
import com.school.erp.dto.EmployeeLeaveDTO;
import com.school.erp.entity.superadmin.EmployeeLeave;
import com.school.erp.entity.SuperAdminEmployee;
import com.school.erp.repository.superadmin.EmployeeLeaveRepository;
import com.school.erp.repository.SuperAdminEmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmployeeLeaveService {

    private final EmployeeLeaveRepository leaveRepository;
    private final SuperAdminEmployeeRepository employeeRepository;

    @Transactional(readOnly = true)
    public List<EmployeeLeaveDTO> getAllLeaves() {
        return leaveRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<EmployeeLeaveDTO> getLeavesByEmployee(Long employeeId) {
        return leaveRepository.findByEmployeeId(employeeId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public EmployeeLeaveDTO applyLeave(ApplyLeaveRequest request) {
        SuperAdminEmployee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        EmployeeLeave leave = new EmployeeLeave();
        leave.setEmployee(employee);
        leave.setLeaveType(request.getLeaveType());
        leave.setStartDate(request.getStartDate());
        leave.setEndDate(request.getEndDate());
        leave.setReason(request.getReason());
        leave.setStatus("PENDING");

        return mapToDTO(leaveRepository.save(leave));
    }

    @Transactional
    public EmployeeLeaveDTO updateLeaveStatus(Long leaveId, String status, Long approvedById) {
        EmployeeLeave leave = leaveRepository.findById(leaveId)
                .orElseThrow(() -> new RuntimeException("Leave record not found"));
        
        SuperAdminEmployee approver = employeeRepository.findById(approvedById)
                .orElseThrow(() -> new RuntimeException("Approver not found"));

        leave.setStatus(status);
        leave.setApprovedBy(approver);

        return mapToDTO(leaveRepository.save(leave));
    }

    private EmployeeLeaveDTO mapToDTO(EmployeeLeave entity) {
        EmployeeLeaveDTO dto = new EmployeeLeaveDTO();
        dto.setId(entity.getId());
        dto.setEmployeeId(entity.getEmployee().getId());
        dto.setEmployeeName(entity.getEmployee().getUser().getName());
        dto.setLeaveType(entity.getLeaveType());
        dto.setStartDate(entity.getStartDate());
        dto.setEndDate(entity.getEndDate());
        dto.setReason(entity.getReason());
        dto.setStatus(entity.getStatus());
        
        if (entity.getApprovedBy() != null) {
            dto.setApprovedById(entity.getApprovedBy().getId());
            dto.setApprovedByName(entity.getApprovedBy().getUser().getName());
        }
        
        return dto;
    }
}
