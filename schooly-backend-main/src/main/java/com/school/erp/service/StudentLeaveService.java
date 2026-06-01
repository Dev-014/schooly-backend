package com.school.erp.service;

import com.school.erp.dto.studentleave.StudentLeaveRequest;
import com.school.erp.dto.studentleave.StudentLeaveResponse;
import com.school.erp.entity.School;
import com.school.erp.entity.Student;
import com.school.erp.entity.StudentLeave;
import com.school.erp.entity.User;
import com.school.erp.exception.BadRequestException;
import com.school.erp.exception.ResourceNotFoundException;
import com.school.erp.repository.SchoolRepository;
import com.school.erp.repository.StudentRepository;
import com.school.erp.repository.StudentLeaveRepository;
import com.school.erp.repository.UserRepository;
import com.school.erp.security.AuthContextService;
import com.school.erp.security.AuthenticatedUser;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class StudentLeaveService {

    private final StudentLeaveRepository studentLeaveRepository;
    private final StudentRepository studentRepository;
    private final SchoolRepository schoolRepository;
    private final UserRepository userRepository;
    private final AuthContextService authContextService;

    public StudentLeaveService(
            StudentLeaveRepository studentLeaveRepository,
            StudentRepository studentRepository,
            SchoolRepository schoolRepository,
            UserRepository userRepository,
            AuthContextService authContextService
    ) {
        this.studentLeaveRepository = studentLeaveRepository;
        this.studentRepository = studentRepository;
        this.schoolRepository = schoolRepository;
        this.userRepository = userRepository;
        this.authContextService = authContextService;
    }

    public StudentLeaveResponse applyLeave(Long studentId, Long schoolId, StudentLeaveRequest request) {
        Long effectiveSchoolId = authContextService.resolveSchoolId(schoolId);
        Student student = getActiveStudent(studentId, effectiveSchoolId);
        School school = getSchool(effectiveSchoolId);

        if (request.startDate().isAfter(request.endDate())) {
            throw new BadRequestException("Start date cannot be after end date");
        }

        StudentLeave studentLeave = new StudentLeave();
        studentLeave.setStudent(student);
        studentLeave.setSchool(school);
        studentLeave.setStartDate(request.startDate());
        studentLeave.setEndDate(request.endDate());
        studentLeave.setReason(request.reason());
        studentLeave.setStatus("PENDING");

        return toResponse(studentLeaveRepository.save(studentLeave));
    }

    public List<StudentLeaveResponse> getLeavesForStudent(Long studentId, Long schoolId) {
        Long effectiveSchoolId = authContextService.resolveSchoolId(schoolId);
        // Ensure student exists
        getActiveStudent(studentId, effectiveSchoolId);

        return studentLeaveRepository.findByStudentIdAndDeletedAtIsNull(studentId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<StudentLeaveResponse> getLeavesForSchool(Long schoolId, String status) {
        Long effectiveSchoolId = authContextService.resolveSchoolId(schoolId);
        List<StudentLeave> leaves = (status != null && !status.isBlank())
                ? studentLeaveRepository.findBySchoolIdAndStatusAndDeletedAtIsNull(effectiveSchoolId, status.toUpperCase())
                : studentLeaveRepository.findBySchoolIdAndDeletedAtIsNull(effectiveSchoolId);
        
        return leaves.stream().map(this::toResponse).toList();
    }

    public StudentLeaveResponse updateLeaveStatus(Long leaveId, Long schoolId, String status) {
        Long effectiveSchoolId = authContextService.resolveSchoolId(schoolId);
        StudentLeave leave = findLeave(leaveId, effectiveSchoolId);

        String upperStatus = status.toUpperCase();
        if (!upperStatus.equals("APPROVED") && !upperStatus.equals("REJECTED") && !upperStatus.equals("PENDING")) {
            throw new BadRequestException("Invalid status: " + status + ". Must be PENDING, APPROVED or REJECTED.");
        }

        AuthenticatedUser currentUser = authContextService.requireCurrentUser();
        User approvingUser = userRepository.findById(currentUser.userId())
                .orElseThrow(() -> new ResourceNotFoundException("Approving user not found for ID " + currentUser.userId()));

        leave.setStatus(upperStatus);
        leave.setApprovedBy(approvingUser);
        leave.setUpdatedAt(LocalDateTime.now());

        return toResponse(studentLeaveRepository.save(leave));
    }

    public void cancelLeave(Long leaveId, Long schoolId) {
        Long effectiveSchoolId = authContextService.resolveSchoolId(schoolId);
        StudentLeave leave = findLeave(leaveId, effectiveSchoolId);
        leave.setDeletedAt(LocalDateTime.now());
        studentLeaveRepository.save(leave);
    }

    private StudentLeave findLeave(Long id, Long schoolId) {
        return studentLeaveRepository.findByIdAndSchoolIdAndDeletedAtIsNull(id, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Student leave record not found for id " + id + " and schoolId " + schoolId
                ));
    }

    private Student getActiveStudent(Long studentId, Long schoolId) {
        return studentRepository.findByIdAndSchoolIdAndDeletedAtIsNull(studentId, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Student not found for id " + studentId + " and schoolId " + schoolId
                ));
    }

    private School getSchool(Long schoolId) {
        return schoolRepository.findById(schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("School not found for id " + schoolId));
    }

    private StudentLeaveResponse toResponse(StudentLeave leave) {
        Long approvedById = null;
        String approvedByName = null;
        if (leave.getApprovedBy() != null) {
            approvedById = leave.getApprovedBy().getId();
            approvedByName = leave.getApprovedBy().getName();
        }

        return new StudentLeaveResponse(
                leave.getId(),
                leave.getStudent().getId(),
                leave.getStudent().getName(),
                leave.getSchool().getId(),
                leave.getStartDate(),
                leave.getEndDate(),
                leave.getReason(),
                leave.getStatus(),
                approvedById,
                approvedByName,
                leave.getCreatedAt()
        );
    }
}
