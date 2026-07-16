package com.school.erp.service;

import com.school.erp.dto.attendance.AttendanceRequest;
import com.school.erp.dto.attendance.AttendanceResponse;
import com.school.erp.entity.Attendance;
import com.school.erp.entity.School;
import com.school.erp.entity.Student;
import com.school.erp.exception.ResourceNotFoundException;
import com.school.erp.repository.AttendanceRepository;
import com.school.erp.repository.SchoolRepository;
import com.school.erp.repository.StudentRepository;
import com.school.erp.security.AuthContextService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final SchoolRepository schoolRepository;
    private final StudentRepository studentRepository;
    private final AuthContextService authContextService;

    public AttendanceService(
            AttendanceRepository attendanceRepository,
            SchoolRepository schoolRepository,
            StudentRepository studentRepository,
            AuthContextService authContextService
    ) {
        this.attendanceRepository = attendanceRepository;
        this.schoolRepository = schoolRepository;
        this.studentRepository = studentRepository;
        this.authContextService = authContextService;
    }

    public List<AttendanceResponse> getAttendance(Long schoolId, Long studentId) {
        Long effectiveSchoolId = authContextService.resolveSchoolId(schoolId);
        List<Attendance> records = studentId == null
                ? attendanceRepository.findBySchoolId(effectiveSchoolId)
                : attendanceRepository.findBySchoolIdAndStudentId(effectiveSchoolId, studentId);
        return records.stream().map(this::toResponse).toList();
    }

    @Transactional
    public AttendanceResponse createAttendance(AttendanceRequest request) {
        Long effectiveSchoolId = authContextService.resolveSchoolId(request.schoolId());
        School school = getSchool(effectiveSchoolId);
        Student student = getStudent(request.studentId(), effectiveSchoolId);

        Attendance attendance = new Attendance();
        attendance.setSchool(school);
        attendance.setStudent(student);
        attendance.setAttendanceDate(request.attendanceDate());
        attendance.setStatus(request.status());

        return toResponse(attendanceRepository.save(attendance));
    }

    private School getSchool(Long schoolId) {
        return schoolRepository.findById(schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("School not found for id " + schoolId));
    }

    private Student getStudent(Long studentId, Long schoolId) {
        return studentRepository.findByIdAndSchoolId(studentId, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Student not found for id " + studentId + " and schoolId " + schoolId
                ));
    }

    private AttendanceResponse toResponse(Attendance attendance) {
        return new AttendanceResponse(
                attendance.getId(),
                attendance.getStudent().getId(),
                attendance.getSchool().getId(),
                attendance.getAttendanceDate(),
                attendance.getStatus()
        );
    }
}
