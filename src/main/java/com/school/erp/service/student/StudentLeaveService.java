package com.school.erp.service.student;

import com.school.erp.dto.academic.ClassTeacherAssignmentResponse;
import com.school.erp.dto.student.StudentLeaveRequest;
import com.school.erp.dto.student.StudentLeaveResponse;
import com.school.erp.entity.auth.UserRole;
import com.school.erp.entity.superadmin.School;
import com.school.erp.entity.student.Student;
import com.school.erp.entity.student.StudentLeave;
import com.school.erp.exception.ForbiddenException;
import com.school.erp.exception.ResourceNotFoundException;
import com.school.erp.repository.superadmin.SchoolRepository;
import com.school.erp.repository.student.StudentLeaveRepository;
import com.school.erp.repository.student.StudentRepository;
import com.school.erp.security.AuthContextService;
import com.school.erp.security.AuthenticatedUser;
import com.school.erp.service.academic.AcademicService;
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
    private final AcademicService academicService;

    public StudentLeaveService(
            StudentLeaveRepository leaveRepository,
            StudentRepository studentRepository,
            SchoolRepository schoolRepository,
            AuthContextService authContextService,
            AcademicService academicService
    ) {
        this.leaveRepository = leaveRepository;
        this.studentRepository = studentRepository;
        this.schoolRepository = schoolRepository;
        this.authContextService = authContextService;
        this.academicService = academicService;
    }

    public List<StudentLeaveResponse> getAllLeaves(Long schoolId, Long studentId) {
        Long effectiveSchoolId = authContextService.resolveSchoolId(schoolId);
        List<StudentLeave> leaves;
        if (studentId != null) {
            leaves = leaveRepository.findBySchoolIdAndStudentId(effectiveSchoolId, studentId);
        } else {
            leaves = leaveRepository.findBySchoolId(effectiveSchoolId);
        }

        AuthenticatedUser currentUser = authContextService.getCurrentUserOrNull();
        if (currentUser != null && currentUser.role() == UserRole.TEACHER) {
            List<ClassTeacherAssignmentResponse> assignments =
                    academicService.getMyClassTeacherAssignments(effectiveSchoolId);
            leaves = leaves.stream()
                    .filter(leave -> {
                        if (leave.getStudent() == null || leave.getStudent().getSchoolClass() == null) {
                            return false;
                        }
                        Long classId = leave.getStudent().getSchoolClass().getId();
                        Long sectionId = leave.getStudent().getSectionId();
                        return assignments.stream().anyMatch(a ->
                                a.classId().equals(classId) && (a.sectionId() == null || a.sectionId().equals(sectionId))
                        );
                    })
                    .toList();
        }

        return leaves.stream()
                .map(this::toResponse)
                .toList();
    }

    public List<StudentLeaveResponse> getAllLeaves(Long schoolId) {
        return getAllLeaves(schoolId, null);
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

        AuthenticatedUser currentUser = authContextService.getCurrentUserOrNull();
        if (currentUser != null && currentUser.role() == UserRole.TEACHER) {
            List<ClassTeacherAssignmentResponse> assignments =
                    academicService.getMyClassTeacherAssignments(effectiveSchoolId);
            Long classId = student.getSchoolClass() != null ? student.getSchoolClass().getId() : null;
            Long sectionId = student.getSectionId();

            boolean isAssigned = classId != null && assignments.stream().anyMatch(a ->
                    a.classId().equals(classId) && (a.sectionId() == null || a.sectionId().equals(sectionId))
            );
            if (!isAssigned) {
                throw new ForbiddenException("Teachers can only apply leaves for students in their assigned class and section");
            }
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

        AuthenticatedUser currentUser = authContextService.getCurrentUserOrNull();
        if (currentUser != null && currentUser.role() == UserRole.TEACHER) {
            List<ClassTeacherAssignmentResponse> assignments =
                    academicService.getMyClassTeacherAssignments(effectiveSchoolId);
            Long classId = leave.getStudent() != null && leave.getStudent().getSchoolClass() != null
                    ? leave.getStudent().getSchoolClass().getId() : null;
            Long sectionId = leave.getStudent() != null ? leave.getStudent().getSectionId() : null;

            boolean isAssigned = classId != null && assignments.stream().anyMatch(a ->
                    a.classId().equals(classId) && (a.sectionId() == null || a.sectionId().equals(sectionId))
            );
            if (!isAssigned) {
                throw new ForbiddenException("Teachers can only approve or reject leaves for their assigned class and section");
            }
        }

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
        Long classId = leave.getStudent().getSchoolClass() != null ? leave.getStudent().getSchoolClass().getId() : null;
        Long sectionId = leave.getStudent().getSectionId();
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
                leave.getCreatedAt(),
                classId,
                sectionId
        );
    }
}
