package com.school.erp.service.hr;

import com.school.erp.dto.hr.BulkStaffAttendanceRequest;
import com.school.erp.dto.hr.StaffAttendanceDTO;
import com.school.erp.dto.hr.StaffAttendanceRequest;
import com.school.erp.dto.hr.StaffAttendanceStatsDTO;
import com.school.erp.entity.hr.Staff;
import com.school.erp.entity.hr.StaffAttendance;
import com.school.erp.repository.hr.StaffRepository;
import com.school.erp.repository.hr.StaffAttendanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StaffAttendanceService {

    private final StaffAttendanceRepository staffAttendanceRepository;
    private final StaffRepository staffRepository;

    @Transactional
    public StaffAttendanceDTO markAttendance(Long schoolId, StaffAttendanceRequest request) {
        Staff staff = staffRepository.findByIdAndSchoolId(request.getStaffId(), schoolId)
                .orElseThrow(() -> new RuntimeException("Staff not found in this school"));

        StaffAttendance attendance = staffAttendanceRepository
                .findByStaffIdAndAttendanceDate(request.getStaffId(), request.getAttendanceDate())
                .orElse(new StaffAttendance());

        attendance.setSchool(staff.getSchool());
        attendance.setStaff(staff);
        attendance.setAttendanceDate(request.getAttendanceDate());
        attendance.setStatus(request.getStatus());
        attendance.setCheckInTime(request.getCheckInTime());
        attendance.setCheckOutTime(request.getCheckOutTime());
        attendance.setNotes(request.getNotes());

        if (request.getCheckInTime() != null && request.getCheckOutTime() != null) {
            long minutes = Duration.between(request.getCheckInTime(), request.getCheckOutTime()).toMinutes();
            BigDecimal hours = BigDecimal.valueOf(minutes).divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
            attendance.setWorkingHours(hours);
        } else {
            attendance.setWorkingHours(null);
        }

        StaffAttendance saved = staffAttendanceRepository.save(attendance);
        return mapToDTO(saved, staff);
    }

    @Transactional
    public List<StaffAttendanceDTO> bulkMarkAttendance(Long schoolId, BulkStaffAttendanceRequest request) {
        if (request.getRecords() == null || request.getRecords().isEmpty()) {
            return List.of();
        }

        List<StaffAttendanceDTO> result = new ArrayList<>();
        for (StaffAttendanceRequest singleReq : request.getRecords()) {
            if (singleReq.getAttendanceDate() == null) {
                singleReq.setAttendanceDate(request.getAttendanceDate());
            }
            result.add(markAttendance(schoolId, singleReq));
        }
        return result;
    }

    @Transactional(readOnly = true)
    public List<StaffAttendanceDTO> getAttendanceByDate(Long schoolId, LocalDate date) {
        return staffAttendanceRepository.findBySchoolIdAndAttendanceDate(schoolId, date)
                .stream()
                .map(a -> mapToDTO(a, a.getStaff()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<StaffAttendanceDTO> getAttendanceRegister(Long schoolId, LocalDate date, Long departmentId, String search) {
        List<Staff> staffList = staffRepository.findBySchoolId(schoolId);

        // Filter by department if specified
        if (departmentId != null) {
            staffList = staffList.stream()
                    .filter(s -> departmentId.equals(s.getDepartmentId()))
                    .collect(Collectors.toList());
        }

        // Filter by search query if specified
        if (search != null && !search.trim().isEmpty()) {
            String q = search.trim().toLowerCase();
            staffList = staffList.stream()
                    .filter(s -> {
                        String fullName = ((s.getFirstName() != null ? s.getFirstName() : "") + " " +
                                (s.getLastName() != null ? s.getLastName() : "")).toLowerCase();
                        String email = s.getEmail() != null ? s.getEmail().toLowerCase() : "";
                        String phone = s.getPhone() != null ? s.getPhone().toLowerCase() : "";
                        return fullName.contains(q) || email.contains(q) || phone.contains(q);
                    })
                    .collect(Collectors.toList());
        }

        // Map of staffId -> existing attendance record for this date
        List<StaffAttendance> attendances = staffAttendanceRepository.findBySchoolIdAndAttendanceDate(schoolId, date);
        Map<Long, StaffAttendance> attendanceMap = attendances.stream()
                .collect(Collectors.toMap(a -> a.getStaff().getId(), a -> a, (a1, a2) -> a1));

        return staffList.stream().map(staff -> {
            StaffAttendance att = attendanceMap.get(staff.getId());
            if (att != null) {
                return mapToDTO(att, staff);
            } else {
                // Return default unmarked attendance DTO for registry
                return StaffAttendanceDTO.builder()
                        .id(null)
                        .schoolId(schoolId)
                        .staffId(staff.getId())
                        .staffName((staff.getFirstName() != null ? staff.getFirstName() : "") + " " +
                                (staff.getLastName() != null ? staff.getLastName() : "").trim())
                        .staffCode(staff.getBiometricId() != null ? staff.getBiometricId() : "STF-" + String.format("%04d", staff.getId()))
                        .department(staff.getDepartment() != null ? staff.getDepartment() : "General")
                        .designation(staff.getDesignation() != null ? staff.getDesignation() : "Staff Member")
                        .photoUrl(staff.getPhotoUrl())
                        .attendanceDate(date)
                        .status("UNMARKED")
                        .checkInTime(null)
                        .checkOutTime(null)
                        .workingHours(null)
                        .notes(null)
                        .build();
            }
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public StaffAttendanceStatsDTO getAttendanceStats(Long schoolId, LocalDate date) {
        List<Staff> staffList = staffRepository.findBySchoolId(schoolId);
        long totalStaff = staffList.size();

        List<StaffAttendance> attendances = staffAttendanceRepository.findBySchoolIdAndAttendanceDate(schoolId, date);

        long presentCount = 0;
        long absentCount = 0;
        long lateCount = 0;
        long halfDayCount = 0;
        long onLeaveCount = 0;

        for (StaffAttendance att : attendances) {
            String status = att.getStatus() != null ? att.getStatus().toUpperCase() : "";
            switch (status) {
                case "PRESENT" -> presentCount++;
                case "ABSENT" -> absentCount++;
                case "LATE" -> lateCount++;
                case "HALF_DAY" -> halfDayCount++;
                case "ON_LEAVE", "LEAVE" -> onLeaveCount++;
            }
        }

        long recordedStaff = presentCount + absentCount + lateCount + halfDayCount + onLeaveCount;
        long unmarkedCount = Math.max(0, totalStaff - recordedStaff);

        double attendancePercentage = totalStaff > 0
                ? BigDecimal.valueOf((presentCount + lateCount + (halfDayCount * 0.5)) * 100.0 / totalStaff)
                .setScale(1, RoundingMode.HALF_UP).doubleValue()
                : 0.0;

        return StaffAttendanceStatsDTO.builder()
                .date(date)
                .totalStaff(totalStaff)
                .presentCount(presentCount)
                .absentCount(absentCount)
                .lateCount(lateCount)
                .halfDayCount(halfDayCount)
                .onLeaveCount(onLeaveCount)
                .unmarkedCount(unmarkedCount)
                .attendancePercentage(attendancePercentage)
                .build();
    }

    private StaffAttendanceDTO mapToDTO(StaffAttendance attendance, Staff staff) {
        Staff effectiveStaff = staff != null ? staff : attendance.getStaff();
        String firstName = effectiveStaff != null && effectiveStaff.getFirstName() != null ? effectiveStaff.getFirstName() : "";
        String lastName = effectiveStaff != null && effectiveStaff.getLastName() != null ? effectiveStaff.getLastName() : "";
        String staffName = (firstName + " " + lastName).trim();

        return StaffAttendanceDTO.builder()
                .id(attendance.getId())
                .schoolId(attendance.getSchool() != null ? attendance.getSchool().getId() : null)
                .staffId(effectiveStaff != null ? effectiveStaff.getId() : null)
                .staffName(!staffName.isEmpty() ? staffName : "Staff #" + (effectiveStaff != null ? effectiveStaff.getId() : ""))
                .staffCode(effectiveStaff != null && effectiveStaff.getBiometricId() != null
                        ? effectiveStaff.getBiometricId()
                        : "STF-" + String.format("%04d", effectiveStaff != null ? effectiveStaff.getId() : 0))
                .department(effectiveStaff != null && effectiveStaff.getDepartment() != null ? effectiveStaff.getDepartment() : "General")
                .designation(effectiveStaff != null && effectiveStaff.getDesignation() != null ? effectiveStaff.getDesignation() : "Staff Member")
                .photoUrl(effectiveStaff != null ? effectiveStaff.getPhotoUrl() : null)
                .attendanceDate(attendance.getAttendanceDate())
                .status(attendance.getStatus())
                .checkInTime(attendance.getCheckInTime())
                .checkOutTime(attendance.getCheckOutTime())
                .workingHours(attendance.getWorkingHours())
                .notes(attendance.getNotes())
                .build();
    }
}

