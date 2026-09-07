package com.school.erp.service.hr;

import com.school.erp.dto.hr.StaffPayrollDTO;
import com.school.erp.dto.hr.StaffPayrollRequest;
import com.school.erp.entity.School;
import com.school.erp.entity.Staff;
import com.school.erp.entity.hr.StaffPayroll;
import com.school.erp.repository.SchoolRepository;
import com.school.erp.repository.StaffRepository;
import com.school.erp.repository.hr.StaffPayrollRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StaffPayrollService {

    private final StaffPayrollRepository staffPayrollRepository;
    private final StaffRepository staffRepository;
    private final SchoolRepository schoolRepository;

    @Transactional
    public StaffPayrollDTO runPayroll(Long schoolId, StaffPayrollRequest request) {
        School school = schoolRepository.findById(schoolId)
                .orElseThrow(() -> new RuntimeException("School not found"));

        Staff staff = staffRepository.findByIdAndSchoolId(request.getStaffId(), schoolId)
                .orElseThrow(() -> new RuntimeException("Staff not found"));

        StaffPayroll payroll = staffPayrollRepository
                .findByStaffIdAndPayrollMonthAndPayrollYear(request.getStaffId(), request.getPayrollMonth(), request.getPayrollYear())
                .orElse(new StaffPayroll());

        payroll.setSchool(school);
        payroll.setStaff(staff);
        payroll.setPayrollMonth(request.getPayrollMonth());
        payroll.setPayrollYear(request.getPayrollYear());
        payroll.setBasicSalary(request.getBasicSalary());
        payroll.setTotalEarnings(request.getTotalEarnings());
        payroll.setTotalDeductions(request.getTotalDeductions());
        payroll.setNetPayableSalary(request.getNetPayableSalary());
        payroll.setStatus("PENDING");

        return mapToDTO(staffPayrollRepository.save(payroll));
    }

    public List<StaffPayrollDTO> getAllPayrolls(Long schoolId) {
        return staffPayrollRepository.findBySchoolId(schoolId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private StaffPayrollDTO mapToDTO(StaffPayroll payroll) {
        StaffPayrollDTO dto = new StaffPayrollDTO();
        dto.setId(payroll.getId());
        dto.setSchoolId(payroll.getSchool().getId());
        dto.setStaffId(payroll.getStaff().getId());
        dto.setStaffName(payroll.getStaff().getFirstName() + " " + payroll.getStaff().getLastName());
        dto.setPayrollMonth(payroll.getPayrollMonth());
        dto.setPayrollYear(payroll.getPayrollYear());
        dto.setBasicSalary(payroll.getBasicSalary());
        dto.setTotalEarnings(payroll.getTotalEarnings());
        dto.setTotalDeductions(payroll.getTotalDeductions());
        dto.setNetPayableSalary(payroll.getNetPayableSalary());
        dto.setStatus(payroll.getStatus());
        dto.setPaymentDate(payroll.getPaymentDate());
        return dto;
    }
}
