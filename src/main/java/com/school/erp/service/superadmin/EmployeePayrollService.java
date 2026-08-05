package com.school.erp.service.superadmin;

import com.school.erp.dto.EmployeePayrollDTO;
import com.school.erp.dto.RunPayrollRequest;
import com.school.erp.entity.SuperAdminEmployee;
import com.school.erp.entity.superadmin.EmployeePayroll;
import com.school.erp.repository.SuperAdminEmployeeRepository;
import com.school.erp.repository.superadmin.EmployeePayrollRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmployeePayrollService {

    private final EmployeePayrollRepository payrollRepository;
    private final SuperAdminEmployeeRepository employeeRepository;

    @Transactional(readOnly = true)
    public List<EmployeePayrollDTO> getAllPayrolls() {
        return payrollRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public EmployeePayrollDTO runPayroll(RunPayrollRequest request) {
        SuperAdminEmployee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new RuntimeException("Employee not found with id: " + request.getEmployeeId()));

        EmployeePayroll payroll = new EmployeePayroll();
        payroll.setEmployee(employee);
        payroll.setMonth(request.getMonth());
        payroll.setYear(request.getYear());
        payroll.setBaseSalary(request.getBaseSalary());
        payroll.setAllowances(request.getAllowances() != null ? request.getAllowances() : BigDecimal.ZERO);
        payroll.setDeductions(request.getDeductions() != null ? request.getDeductions() : BigDecimal.ZERO);
        
        BigDecimal netSalary = payroll.getBaseSalary()
                .add(payroll.getAllowances())
                .subtract(payroll.getDeductions());
        payroll.setNetSalary(netSalary);
        
        payroll.setStatus(request.getStatus() != null ? request.getStatus() : "PENDING");
        payroll.setPaymentDate(request.getPaymentDate());

        EmployeePayroll savedPayroll = payrollRepository.save(payroll);
        return mapToDTO(savedPayroll);
    }
    
    @Transactional
    public EmployeePayrollDTO updatePayrollStatus(Long payrollId, String status) {
        EmployeePayroll payroll = payrollRepository.findById(payrollId)
                .orElseThrow(() -> new RuntimeException("Payroll not found with id: " + payrollId));
                
        payroll.setStatus(status);
        EmployeePayroll updatedPayroll = payrollRepository.save(payroll);
        return mapToDTO(updatedPayroll);
    }

    private EmployeePayrollDTO mapToDTO(EmployeePayroll payroll) {
        EmployeePayrollDTO dto = new EmployeePayrollDTO();
        dto.setId(payroll.getId());
        dto.setEmployeeId(payroll.getEmployee().getId());
        dto.setEmployeeName(payroll.getEmployee().getUser().getName());
        dto.setMonth(payroll.getMonth());
        dto.setYear(payroll.getYear());
        dto.setBaseSalary(payroll.getBaseSalary());
        dto.setAllowances(payroll.getAllowances());
        dto.setDeductions(payroll.getDeductions());
        dto.setNetSalary(payroll.getNetSalary());
        dto.setStatus(payroll.getStatus());
        dto.setPaymentDate(payroll.getPaymentDate());
        return dto;
    }
}
