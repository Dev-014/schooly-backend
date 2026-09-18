package com.school.erp.service;

import com.school.erp.dto.student.StudentLeaveRequest;
import com.school.erp.dto.student.StudentLeaveResponse;
import com.school.erp.entity.School;
import com.school.erp.entity.Student;
import com.school.erp.entity.StudentLeave;
import com.school.erp.exception.ResourceNotFoundException;
import com.school.erp.repository.SchoolRepository;
import com.school.erp.repository.StudentLeaveRepository;
import com.school.erp.repository.StudentRepository;
import com.school.erp.security.AuthContextService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class StudentLeaveService {

    private final StudentLeaveRepository leaveRepository;
    private final StudentRepository studentRepository;
    private final SchoolRepository schoolRepository;
    private final AuthContextService authContextService;

    public StudentLeaveService(StudentLeaveRepository leaveRepository, StudentRepository studentRepository, SchoolRepository schoolRepository, AuthContextService authContextService) {
        this.leaveRepository = leaveRepository;
        this.studentRepository = studentRepository;
        this.schoolRepository = schoolRepository;
        this.authContextService = authContextService;
    }

    public List<StudentLeaveResponse> getAllLeaves(Long schoolId) {
        Long effectiveSchoolId = authContextService.resolveSchoolId(schoolId);
        return leaveRepository.findBySchoolId(effectiveSchoolId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public StudentLeaveResponse createLeave(StudentLeaveRequest request) {
        Long effectiveSchoolId = authContextService.resolveSchoolId(request.schoolId());
        School school = schoolRepository.findById(effectiveSchoolId)
                .orElseThrow(() -> new ResourceNotFoundException("School not found"));

        Student student = studentRepository.findById(request.studentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        
        if (!student.getSchool().getId().equals(effectiveSchoolId)) {
            throw new IllegalArgumentException("Student does not belong to this school");
        }

        StudentLeave leave = new StudentLeave();
        leave.setSchool(school);
        leave.setStudent(student);
        leave.setApplyDate(LocalDate.now());
        leave.setFromDate(request.fromDate());
        leave.setToDate(request.toDate());
        leave.setDays(request.days());
        leave.setReason(request.reason());
        
        if (request.status() != null && !request.status().isBlank()) {
            leave.setStatus(request.status());
        }
        
        return toResponse(leaveRepository.save(leave));
    }

    @Transactional
    public StudentLeaveResponse updateLeave(Long id, StudentLeaveRequest request) {
        Long effectiveSchoolId = authContextService.resolveSchoolId(request.schoolId());
        StudentLeave leave = leaveRepository.findByIdAndSchoolId(id, effectiveSchoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Leave not found"));

        leave.setFromDate(request.fromDate());
        leave.setToDate(request.toDate());
        leave.setDays(request.days());
        leave.setReason(request.reason());
        leave.setReply(request.reply());
        
        if (request.status() != null && !request.status().isBlank()) {
            leave.setStatus(request.status());
        }

        return toResponse(leaveRepository.save(leave));
    }

    @Transactional
    public void deleteLeave(Long id, Long schoolId) {
        Long effectiveSchoolId = authContextService.resolveSchoolId(schoolId);
        StudentLeave leave = leaveRepository.findByIdAndSchoolId(id, effectiveSchoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Leave not found"));
        leaveRepository.delete(leave);
    }

    private StudentLeaveResponse toResponse(StudentLeave leave) {
        String classKey = leave.getStudent().getSchoolClass() != null ? leave.getStudent().getSchoolClass().getName() : "";
        return new StudentLeaveResponse(
                leave.getId(),
                leave.getSchool().getId(),
                leave.getStudent().getId(),
                leave.getStudent().getFirstName() + " " + leave.getStudent().getLastName(),
                leave.getStudent().getAdmissionNo(),
                classKey,
                leave.getApplyDate(),
                leave.getFromDate(),
                leave.getToDate(),
                leave.getDays(),
                leave.getReason(),
                leave.getStatus(),
                leave.getReply(),
                leave.getCreatedAt()
        );
    }
}
