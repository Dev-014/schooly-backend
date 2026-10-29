package com.school.erp.service.student;

import com.school.erp.dto.academic.ClassTeacherAssignmentResponse;
import com.school.erp.dto.student.StudentLeaveRequest;
import com.school.erp.dto.student.StudentLeaveResponse;
import com.school.erp.entity.auth.UserRole;
import com.school.erp.entity.notification.NotificationPriority;
import com.school.erp.entity.notification.NotificationType;
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
import com.school.erp.service.notification.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class StudentLeaveService {

    private final StudentLeaveRepository leaveRepository;
    private final StudentRepository studentRepository;
    private final SchoolRepository schoolRepository;
    private final AuthContextService authContextService;
    private final AcademicService academicService;

    @Autowired(required = false)
    private NotificationService notificationService;

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
        
        StudentLeave saved = leaveRepository.save(leave);

        if (notificationService != null) {
            String title = "Student Leave Request: " + student.getName();
            String message = student.getName() + " requested leave from " + saved.getFromDate() + " to " + saved.getToDate()
                    + (saved.getReason() != null && !saved.getReason().isBlank() ? " (Reason: " + saved.getReason() + ")." : ".");
            Map<String, Object> metadata = Map.of(
                    "leaveId", saved.getId(),
                    "studentId", student.getId(),
                    "studentName", student.getName(),
                    "fromDate", saved.getFromDate().toString(),
                    "toDate", saved.getToDate().toString()
            );

            notificationService.notifyClassTeachersOfStudent(
                    effectiveSchoolId,
                    student,
                    NotificationType.STUDENT_LEAVE_REQUESTED,
                    NotificationPriority.HIGH,
                    title,
                    message,
                    "/students/leaves",
                    "Review Leave",
                    "STUDENT_LEAVE",
                    String.valueOf(saved.getId()),
                    metadata
            );

            notificationService.notifySchoolAdmins(
                    effectiveSchoolId,
                    NotificationType.STUDENT_LEAVE_REQUESTED,
                    NotificationPriority.MEDIUM,
                    title,
                    message,
                    "/students/leaves",
                    "Review Leave",
                    "STUDENT_LEAVE",
                    String.valueOf(saved.getId()),
                    metadata
            );
        }

        return toResponse(saved);
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

        String oldStatus = leave.getStatus();
        leave.setFromDate(request.fromDate());
        leave.setToDate(request.toDate());
        leave.setDays(request.days());
        leave.setReason(request.reason());
        leave.setReply(request.reply());
        
        if (request.status() != null && !request.status().isBlank()) {
            leave.setStatus(request.status());
        }

        StudentLeave saved = leaveRepository.save(leave);

        if (notificationService != null && saved.getStudent() != null && saved.getStatus() != null
                && !saved.getStatus().equalsIgnoreCase(oldStatus)) {
            notificationService.notifyStudentAndParents(
                    effectiveSchoolId,
                    saved.getStudent(),
                    NotificationType.STUDENT_LEAVE_STATUS_UPDATED,
                    NotificationPriority.HIGH,
                    "Student Leave " + saved.getStatus() + ": " + saved.getStudent().getName(),
                    "Leave request for " + saved.getStudent().getName() + " (" + saved.getFromDate() + " to " + saved.getToDate()
                            + ") has been marked as " + saved.getStatus()
                            + (saved.getReply() != null && !saved.getReply().isBlank() ? ". Remark: " + saved.getReply() : "."),
                    "/students/leaves",
                    "View Leave Status",
                    "STUDENT_LEAVE",
                    String.valueOf(saved.getId()),
                    Map.of(
                            "leaveId", saved.getId(),
                            "studentId", saved.getStudent().getId(),
                            "status", saved.getStatus()
                    )
            );
        }

        return toResponse(saved);
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
