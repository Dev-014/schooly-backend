package com.school.erp.service.superadmin;

import com.school.erp.dto.EmployeeAttendanceDTO;
import com.school.erp.dto.MarkAttendanceRequest;
import com.school.erp.entity.superadmin.EmployeeAttendance;
import com.school.erp.entity.SuperAdminEmployee;
import com.school.erp.repository.superadmin.EmployeeAttendanceRepository;
import com.school.erp.repository.SuperAdminEmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmployeeAttendanceService {

    private final EmployeeAttendanceRepository attendanceRepository;
    private final SuperAdminEmployeeRepository employeeRepository;

    @Transactional(readOnly = true)
    public List<EmployeeAttendanceDTO> getAttendanceByDate(LocalDate date) {
        return attendanceRepository.findByDate(date).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<EmployeeAttendanceDTO> getAttendanceByEmployee(Long employeeId) {
        return attendanceRepository.findByEmployeeId(employeeId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public EmployeeAttendanceDTO markAttendance(MarkAttendanceRequest request) {
        SuperAdminEmployee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        EmployeeAttendance attendance = attendanceRepository.findByEmployeeIdAndDate(request.getEmployeeId(), request.getDate())
                .orElse(new EmployeeAttendance());

        attendance.setEmployee(employee);
        attendance.setDate(request.getDate());
        attendance.setStatus(request.getStatus());
        
        if (request.getCheckInTime() != null) {
            attendance.setCheckInTime(request.getCheckInTime());
        }
        if (request.getCheckOutTime() != null) {
            attendance.setCheckOutTime(request.getCheckOutTime());
        }
        if (request.getNotes() != null) {
            attendance.setNotes(request.getNotes());
        }

        return mapToDTO(attendanceRepository.save(attendance));
    }

    private EmployeeAttendanceDTO mapToDTO(EmployeeAttendance entity) {
        EmployeeAttendanceDTO dto = new EmployeeAttendanceDTO();
        dto.setId(entity.getId());
        dto.setEmployeeId(entity.getEmployee().getId());
        dto.setEmployeeName(entity.getEmployee().getUser().getName());
        dto.setDate(entity.getDate());
        dto.setStatus(entity.getStatus());
        dto.setCheckInTime(entity.getCheckInTime());
        dto.setCheckOutTime(entity.getCheckOutTime());
        dto.setNotes(entity.getNotes());
        return dto;
    }
}
