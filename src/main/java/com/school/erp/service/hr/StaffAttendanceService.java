package com.school.erp.service.hr;

import com.school.erp.dto.hr.StaffAttendanceDTO;
import com.school.erp.dto.hr.StaffAttendanceRequest;
import com.school.erp.entity.Staff;
import com.school.erp.entity.hr.StaffAttendance;
import com.school.erp.repository.StaffRepository;
import com.school.erp.repository.hr.StaffAttendanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
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

        attendance.setSchool(staff.getSchool()); // Use staff's school reference to avoid fetching school from DB again
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
        }

        StaffAttendance saved = staffAttendanceRepository.save(attendance);
        return mapToDTO(saved);
    }

    public List<StaffAttendanceDTO> getAttendanceByDate(Long schoolId, LocalDate date) {
        return staffAttendanceRepository.findBySchoolIdAndAttendanceDate(schoolId, date)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private StaffAttendanceDTO mapToDTO(StaffAttendance attendance) {
        StaffAttendanceDTO dto = new StaffAttendanceDTO();
        dto.setId(attendance.getId());
        dto.setSchoolId(attendance.getSchool().getId());
        dto.setStaffId(attendance.getStaff().getId());
        dto.setStaffName(attendance.getStaff().getFirstName() + " " + attendance.getStaff().getLastName());
        dto.setAttendanceDate(attendance.getAttendanceDate());
        dto.setStatus(attendance.getStatus());
        dto.setCheckInTime(attendance.getCheckInTime());
        dto.setCheckOutTime(attendance.getCheckOutTime());
        dto.setWorkingHours(attendance.getWorkingHours());
        dto.setNotes(attendance.getNotes());
        return dto;
    }
}
