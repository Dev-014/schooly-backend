package com.school.erp.service.hr;

import com.school.erp.dto.hr.*;
import com.school.erp.entity.hr.*;
import com.school.erp.entity.superadmin.School;
import com.school.erp.repository.hr.*;
import com.school.erp.repository.superadmin.SchoolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StaffPayrollService {

    private final StaffPayrollRepository staffPayrollRepository;
    private final StaffPayrollDetailsRepository staffPayrollDetailsRepository;
    private final StaffBankAccountRepository staffBankAccountRepository;
    private final StaffAdvanceRepository staffAdvanceRepository;
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

    @Transactional
    public List<StaffPayrollDTO> generateBatchPayroll(Long schoolId, String payrollMonth, Integer payrollYear) {
        School school = schoolRepository.findById(schoolId)
                .orElseThrow(() -> new RuntimeException("School not found"));

        List<Staff> staffList = staffRepository.findBySchoolId(schoolId);
        List<StaffPayrollDTO> result = new ArrayList<>();

        for (Staff staff : staffList) {
            StaffPayrollDetails details = staffPayrollDetailsRepository.findByStaffId(staff.getId());

            BigDecimal basic;
            BigDecimal totalEarnings;
            BigDecimal totalDeductions;

            if (details != null && details.getBasicSalary() != null) {
                basic = details.getBasicSalary();
                BigDecimal hra = details.getHra() != null ? details.getHra() : BigDecimal.ZERO;
                BigDecimal da = details.getDa() != null ? details.getDa() : BigDecimal.ZERO;
                BigDecimal ta = details.getTa() != null ? details.getTa() : BigDecimal.ZERO;
                BigDecimal special = details.getSpecialAllowance() != null ? details.getSpecialAllowance() : BigDecimal.ZERO;
                totalEarnings = basic.add(hra).add(da).add(ta).add(special);

                BigDecimal pf = details.getPf() != null ? details.getPf() : BigDecimal.ZERO;
                BigDecimal tds = details.getTds() != null ? details.getTds() : BigDecimal.ZERO;
                BigDecimal esic = details.getEsic() != null ? details.getEsic() : BigDecimal.ZERO;
                BigDecimal otherDed = details.getOtherDeductions() != null ? details.getOtherDeductions() : BigDecimal.ZERO;
                totalDeductions = pf.add(tds).add(esic).add(otherDed);
            } else {
                basic = staff.getSalary() != null ? staff.getSalary() : BigDecimal.valueOf(3000);
                BigDecimal allowance = basic.multiply(BigDecimal.valueOf(0.15)).setScale(2, RoundingMode.HALF_UP);
                BigDecimal deduction = basic.multiply(BigDecimal.valueOf(0.08)).setScale(2, RoundingMode.HALF_UP);
                totalEarnings = basic.add(allowance);
                totalDeductions = deduction;
            }

            BigDecimal net = totalEarnings.subtract(totalDeductions);

            StaffPayroll payroll = staffPayrollRepository
                    .findByStaffIdAndPayrollMonthAndPayrollYear(staff.getId(), payrollMonth, payrollYear)
                    .orElse(new StaffPayroll());

            payroll.setSchool(school);
            payroll.setStaff(staff);
            payroll.setPayrollMonth(payrollMonth);
            payroll.setPayrollYear(payrollYear);
            payroll.setBasicSalary(basic);
            payroll.setTotalEarnings(totalEarnings);
            payroll.setTotalDeductions(totalDeductions);
            payroll.setNetPayableSalary(net);
            if (payroll.getStatus() == null) {
                payroll.setStatus("PENDING");
            }

            result.add(mapToDTO(staffPayrollRepository.save(payroll)));
        }

        return result;
    }

    @Transactional(readOnly = true)
    public StaffPayrollSummaryDTO getPayrollSummary(Long schoolId, String payrollMonth, Integer payrollYear) {
        List<StaffPayroll> payrolls = (payrollMonth != null && payrollYear != null)
                ? staffPayrollRepository.findBySchoolIdAndPayrollMonthAndPayrollYear(schoolId, payrollMonth, payrollYear)
                : staffPayrollRepository.findBySchoolId(schoolId);

        BigDecimal totalPayout = BigDecimal.ZERO;
        BigDecimal pendingSalaries = BigDecimal.ZERO;
        BigDecimal totalDeductions = BigDecimal.ZERO;
        long staffPaidCount = 0;
        long staffPendingCount = 0;

        for (StaffPayroll p : payrolls) {
            BigDecimal net = p.getNetPayableSalary() != null ? p.getNetPayableSalary() : BigDecimal.ZERO;
            BigDecimal ded = p.getTotalDeductions() != null ? p.getTotalDeductions() : BigDecimal.ZERO;
            totalPayout = totalPayout.add(net);
            totalDeductions = totalDeductions.add(ded);

            if ("PAID".equalsIgnoreCase(p.getStatus())) {
                staffPaidCount++;
            } else {
                pendingSalaries = pendingSalaries.add(net);
                staffPendingCount++;
            }
        }

        long totalStaff = payrolls.size();
        double paidPct = totalStaff > 0
                ? BigDecimal.valueOf((staffPaidCount * 100.0) / totalStaff).setScale(1, RoundingMode.HALF_UP).doubleValue()
                : 0.0;

        return StaffPayrollSummaryDTO.builder()
                .schoolId(schoolId)
                .payrollMonth(payrollMonth)
                .payrollYear(payrollYear)
                .totalPayout(totalPayout)
                .pendingSalaries(pendingSalaries)
                .totalDeductions(totalDeductions)
                .totalStaff(totalStaff)
                .staffPaidCount(staffPaidCount)
                .staffPendingCount(staffPendingCount)
                .paidPercentage(paidPct)
                .build();
    }

    @Transactional(readOnly = true)
    public StaffPayslipDTO getPayslip(Long schoolId, Long payrollId) {
        StaffPayroll payroll = staffPayrollRepository.findById(payrollId)
                .filter(p -> p.getSchool().getId().equals(schoolId))
                .orElseThrow(() -> new RuntimeException("Payroll record not found"));

        Staff staff = payroll.getStaff();
        StaffPayrollDetails details = staffPayrollDetailsRepository.findByStaffId(staff.getId());
        StaffBankAccount bankAccount = staffBankAccountRepository.findByStaffId(staff.getId()).orElse(null);

        String schoolName = payroll.getSchool() != null ? payroll.getSchool().getName() : "School";
        String staffName = ((staff.getFirstName() != null ? staff.getFirstName() : "") + " " +
                (staff.getLastName() != null ? staff.getLastName() : "")).trim();
        String staffCode = staff.getBiometricId() != null ? staff.getBiometricId() : "STF-" + String.format("%04d", staff.getId());

        BigDecimal hra = details != null && details.getHra() != null ? details.getHra() : BigDecimal.ZERO;
        BigDecimal da = details != null && details.getDa() != null ? details.getDa() : BigDecimal.ZERO;
        BigDecimal ta = details != null && details.getTa() != null ? details.getTa() : BigDecimal.ZERO;
        BigDecimal special = details != null && details.getSpecialAllowance() != null ? details.getSpecialAllowance() : BigDecimal.ZERO;

        BigDecimal pf = details != null && details.getPf() != null ? details.getPf() : BigDecimal.ZERO;
        String epfNumber = details != null ? details.getEpfNumber() : null;
        BigDecimal tds = details != null && details.getTds() != null ? details.getTds() : BigDecimal.ZERO;
        BigDecimal esic = details != null && details.getEsic() != null ? details.getEsic() : BigDecimal.ZERO;
        BigDecimal otherDed = details != null && details.getOtherDeductions() != null ? details.getOtherDeductions() : BigDecimal.ZERO;

        return StaffPayslipDTO.builder()
                .payrollId(payroll.getId())
                .schoolId(schoolId)
                .schoolName(schoolName)
                .staffId(staff.getId())
                .staffName(staffName)
                .staffCode(staffCode)
                .department(staff.getDepartment() != null ? staff.getDepartment() : "General")
                .designation(staff.getDesignation() != null ? staff.getDesignation() : "Staff Member")
                .bankAccount(bankAccount != null ? bankAccount.getAccountNumber() : null)
                .bankName(bankAccount != null ? bankAccount.getBankName() : null)
                .ifscCode(bankAccount != null ? bankAccount.getIfscCode() : null)
                .payrollMonth(payroll.getPayrollMonth())
                .payrollYear(payroll.getPayrollYear())
                .basicSalary(payroll.getBasicSalary())
                .hra(hra)
                .da(da)
                .ta(ta)
                .specialAllowance(special)
                .totalEarnings(payroll.getTotalEarnings())
                .pf(pf)
                .epfNumber(epfNumber)
                .tds(tds)
                .esic(esic)
                .otherDeductions(otherDed)
                .totalDeductions(payroll.getTotalDeductions())
                .netPayableSalary(payroll.getNetPayableSalary())
                .status(payroll.getStatus())
                .paymentDate(payroll.getPaymentDate())
                .build();
    }

    @Transactional
    public StaffPayrollDTO updatePayrollStatus(Long schoolId, Long payrollId, String status, LocalDate paymentDate) {
        StaffPayroll payroll = staffPayrollRepository.findById(payrollId)
                .filter(p -> p.getSchool().getId().equals(schoolId))
                .orElseThrow(() -> new RuntimeException("Payroll record not found"));

        payroll.setStatus(status != null ? status.toUpperCase() : "PENDING");
        if (paymentDate != null) {
            payroll.setPaymentDate(paymentDate);
        } else if ("PAID".equalsIgnoreCase(status) && payroll.getPaymentDate() == null) {
            payroll.setPaymentDate(LocalDate.now());
        }

        return mapToDTO(staffPayrollRepository.save(payroll));
    }

    // Salary Advances
    @Transactional(readOnly = true)
    public List<StaffAdvanceDTO> getAdvances(Long schoolId, Long staffId, String status) {
        List<StaffAdvance> advances = (staffId != null)
                ? staffAdvanceRepository.findByStaffId(staffId)
                : staffAdvanceRepository.findBySchoolId(schoolId);

        if (status != null && !status.trim().isEmpty()) {
            advances = advances.stream()
                    .filter(a -> a.getStatus() != null && a.getStatus().equalsIgnoreCase(status.trim()))
                    .collect(Collectors.toList());
        }

        return advances.stream().map(this::mapAdvanceToDTO).collect(Collectors.toList());
    }

    @Transactional
    public StaffAdvanceDTO issueAdvance(Long schoolId, StaffAdvanceRequest request) {
        School school = schoolRepository.findById(schoolId)
                .orElseThrow(() -> new RuntimeException("School not found"));

        Staff staff = staffRepository.findByIdAndSchoolId(request.getStaffId(), schoolId)
                .orElseThrow(() -> new RuntimeException("Staff not found"));

        StaffAdvance advance = new StaffAdvance();
        advance.setSchool(school);
        advance.setStaff(staff);
        advance.setAdvanceDate(request.getAdvanceDate());
        advance.setAmount(request.getAmount());
        advance.setReason(request.getReason());
        advance.setRepaymentMethod(request.getRepaymentMethod());
        advance.setStatus("PENDING");
        advance.setRecoveredAmount(BigDecimal.ZERO);

        return mapAdvanceToDTO(staffAdvanceRepository.save(advance));
    }

    @Transactional
    public StaffAdvanceDTO updateAdvanceStatus(Long schoolId, Long advanceId, String status, BigDecimal recoveredAmount) {
        StaffAdvance advance = staffAdvanceRepository.findById(advanceId)
                .filter(a -> a.getSchool().getId().equals(schoolId))
                .orElseThrow(() -> new RuntimeException("Advance record not found"));

        if (status != null && !status.trim().isEmpty()) {
            advance.setStatus(status.toUpperCase());
        }
        if (recoveredAmount != null) {
            advance.setRecoveredAmount(recoveredAmount);
        }

        return mapAdvanceToDTO(staffAdvanceRepository.save(advance));
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
        String staffName = ((payroll.getStaff().getFirstName() != null ? payroll.getStaff().getFirstName() : "") + " " +
                (payroll.getStaff().getLastName() != null ? payroll.getStaff().getLastName() : "")).trim();
        dto.setStaffName(staffName);
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

    private StaffAdvanceDTO mapAdvanceToDTO(StaffAdvance advance) {
        String staffName = advance.getStaff() != null
                ? ((advance.getStaff().getFirstName() != null ? advance.getStaff().getFirstName() : "") + " " +
                (advance.getStaff().getLastName() != null ? advance.getStaff().getLastName() : "")).trim()
                : "Staff #" + (advance.getStaff() != null ? advance.getStaff().getId() : "");

        return StaffAdvanceDTO.builder()
                .id(advance.getId())
                .schoolId(advance.getSchool().getId())
                .staffId(advance.getStaff().getId())
                .staffName(staffName)
                .advanceDate(advance.getAdvanceDate())
                .amount(advance.getAmount())
                .reason(advance.getReason())
                .repaymentMethod(advance.getRepaymentMethod())
                .status(advance.getStatus())
                .recoveredAmount(advance.getRecoveredAmount())
                .build();
    }
}
