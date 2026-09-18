package com.school.erp.service.impl;

import com.school.erp.dto.payment.FeeReminderResponse;
import com.school.erp.dto.payment.SendFeeReminderRequest;
import com.school.erp.dto.payment.TeacherClassResponse;
import com.school.erp.entity.ClassTeacherAssignment;
import com.school.erp.entity.FeeReminder;
import com.school.erp.entity.School;
import com.school.erp.entity.Staff;
import com.school.erp.entity.Student;
import com.school.erp.exception.ResourceNotFoundException;
import com.school.erp.repository.ClassTeacherAssignmentRepository;
import com.school.erp.repository.FeeReminderRepository;
import com.school.erp.repository.FeeDueRepository;
import com.school.erp.repository.SchoolRepository;
import com.school.erp.repository.StaffRepository;
import com.school.erp.repository.StudentRepository;
import com.school.erp.repository.SchoolClassRepository;
import com.school.erp.repository.SectionRepository;
import com.school.erp.repository.auth.UserAssignmentRepository;
import com.school.erp.entity.auth.UserAssignment;
import com.school.erp.entity.SchoolClass;
import com.school.erp.entity.Section;
import com.school.erp.security.AuthContextService;
import com.school.erp.security.AuthenticatedUser;
import com.school.erp.service.TeacherFeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeacherFeeServiceImpl implements TeacherFeeService {

    private final ClassTeacherAssignmentRepository classTeacherAssignmentRepository;
    private final StaffRepository staffRepository;
    private final FeeReminderRepository feeReminderRepository;
    private final SchoolRepository schoolRepository;
    private final StudentRepository studentRepository;
    private final AuthContextService authContextService;
    private final UserAssignmentRepository userAssignmentRepository;
    private final SchoolClassRepository schoolClassRepository;
    private final SectionRepository sectionRepository;
    private final FeeDueRepository feeDueRepository;

    @Override
    @Transactional(readOnly = true)
    public List<TeacherClassResponse> getAssignedClasses(Long schoolId) {
        AuthenticatedUser currentUser = authContextService.requireCurrentUser();
        Staff staff = staffRepository.findByUserId(currentUser.userId())
                .orElseThrow(() -> new ResourceNotFoundException("Staff record not found for user"));

        // Fetch from legacy assignment
        List<ClassTeacherAssignment> legacyAssignments = classTeacherAssignmentRepository
                .findByStaffIdAndStatus(staff.getId(), "ACTIVE");

        List<TeacherClassResponse> responses = legacyAssignments.stream()
                .filter(a -> a.getSchool().getId().equals(schoolId))
                .map(a -> new TeacherClassResponse(
                        a.getId(),
                        a.getSchoolClass().getId(),
                        a.getSchoolClass().getName(),
                        a.getSection() != null ? a.getSection().getId() : null,
                        a.getSection() != null ? a.getSection().getName() : null
                ))
                .collect(Collectors.toList());

        // Fetch from new Centralized Authorization assignments
        if (staff.getUserId() != null) {
            List<UserAssignment> userAssignments = userAssignmentRepository
                    .findBySchoolIdAndUserIdAndIsActiveTrue(schoolId, staff.getUserId());
            
            for (UserAssignment ua : userAssignments) {
                if ("class_teacher".equals(ua.getAssignmentType()) && ua.getClassId() != null) {
                    SchoolClass sc = schoolClassRepository.findById(ua.getClassId()).orElse(null);
                    Section sec = ua.getSectionId() != null ? sectionRepository.findById(ua.getSectionId()).orElse(null) : null;
                    
                    if (sc != null) {
                        responses.add(new TeacherClassResponse(
                                ua.getId(),
                                sc.getId(),
                                sc.getName(),
                                sec != null ? sec.getId() : null,
                                sec != null ? sec.getName() : null
                        ));
                    }
                }
            }
        }

        return responses;
    }

    @Override
    @Transactional
    public void sendReminders(Long schoolId, SendFeeReminderRequest request) {
        AuthenticatedUser currentUser = authContextService.requireCurrentUser();
        Staff staff = staffRepository.findByUserId(currentUser.userId())
                .orElse(null); // Super admins might not have staff record

        School school = schoolRepository.findById(schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("School not found"));

        for (Long studentId : request.getStudentIds()) {
            Student student = studentRepository.findByIdAndSchoolId(studentId, schoolId)
                    .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

            List<com.school.erp.entity.FeeDue> dues = feeDueRepository.findByStudentIdAndSchoolIdAndStatusInOrderByDueDateAsc(
                    studentId, schoolId, List.of("UNPAID", "PARTIALLY_PAID", "OVERDUE"));
            BigDecimal totalPending = dues.stream()
                    .map(d -> d.getAmount().subtract(d.getPaidAmount() != null ? d.getPaidAmount() : BigDecimal.ZERO))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            FeeReminder reminder = new FeeReminder();
            reminder.setSchool(school);
            reminder.setStaff(staff);
            reminder.setStudent(student);
            reminder.setFeeAmount(totalPending);
            reminder.setMethod(request.getMethod());
            reminder.setStatus("SENT");
            reminder.setSentAt(LocalDateTime.now());
            
            feeReminderRepository.save(reminder);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FeeReminderResponse> getReminderHistory(Long schoolId, Pageable pageable) {
        AuthenticatedUser currentUser = authContextService.requireCurrentUser();
        Staff staff = staffRepository.findByUserId(currentUser.userId())
                .orElse(null);

        Page<FeeReminder> reminders;
        if (staff != null) {
            reminders = feeReminderRepository.findBySchoolIdAndStaffId(schoolId, staff.getId(), pageable);
        } else {
            reminders = feeReminderRepository.findBySchoolId(schoolId, pageable);
        }

        return reminders.map(r -> {
            Student student = r.getStudent();
            String studentName = "";
            if (student != null) {
                if (student.getFirstName() != null) {
                    studentName = student.getFirstName() + (student.getLastName() != null && !student.getLastName().isBlank() ? " " + student.getLastName() : "");
                } else if (student.getName() != null && !student.getName().isBlank()) {
                    studentName = student.getName();
                } else {
                    studentName = "Student " + (student.getAdmissionNo() != null ? student.getAdmissionNo() : student.getId());
                }
            }

            return FeeReminderResponse.builder()
                .id(r.getId())
                .studentId(student != null ? student.getId() : null)
                .studentName(studentName)
                .parentName(student != null && student.getGuardianName() != null ? student.getGuardianName() : "Guardian")
                .feeAmount(r.getFeeAmount())
                .method(r.getMethod())
                .status(r.getStatus())
                .sentAt(r.getSentAt())
                .build();
        });
    }
}
