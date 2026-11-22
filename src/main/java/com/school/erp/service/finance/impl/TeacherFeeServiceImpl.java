package com.school.erp.service.finance.impl;

import com.school.erp.dto.payment.FeeReminderResponse;
import com.school.erp.dto.payment.SendFeeReminderRequest;
import com.school.erp.dto.payment.TeacherClassResponse;
import com.school.erp.entity.academic.ClassTeacherAssignment;
import com.school.erp.entity.finance.FeeReminder;
import com.school.erp.entity.notification.NotificationPriority;
import com.school.erp.entity.notification.NotificationType;
import com.school.erp.entity.superadmin.School;
import com.school.erp.entity.hr.Staff;
import com.school.erp.entity.student.Student;
import com.school.erp.exception.ResourceNotFoundException;
import com.school.erp.repository.academic.ClassTeacherAssignmentRepository;
import com.school.erp.repository.finance.FeeReminderRepository;
import com.school.erp.repository.finance.FeeDueRepository;
import com.school.erp.repository.superadmin.SchoolRepository;
import com.school.erp.repository.hr.StaffRepository;
import com.school.erp.repository.student.StudentRepository;
import com.school.erp.repository.academic.SchoolClassRepository;
import com.school.erp.repository.academic.SectionRepository;
import com.school.erp.repository.auth.UserAssignmentRepository;
import com.school.erp.entity.auth.RolePermission;
import com.school.erp.entity.auth.UserAssignment;
import com.school.erp.entity.auth.UserRole;
import com.school.erp.entity.academic.SchoolClass;
import com.school.erp.entity.academic.Section;
import com.school.erp.security.AuthContextService;
import com.school.erp.security.AuthenticatedUser;
import com.school.erp.service.auth.AuthorizationService;
import com.school.erp.service.finance.TeacherFeeService;
import com.school.erp.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
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

    @Autowired(required = false)
    private NotificationService notificationService;

    @Autowired(required = false)
    @Lazy
    private AuthorizationService authorizationService;

    private boolean hasSchoolReminderScope(Long schoolId, AuthenticatedUser currentUser) {
        if (currentUser == null) return false;
        if (currentUser.role() == UserRole.SUPER_ADMIN
                || currentUser.role() == UserRole.ADMIN) {
            return true;
        }
        if (authorizationService == null) return false;
        try {
            List<RolePermission> effective = authorizationService.getEffectivePermissions(schoolId, currentUser.userId());
            return effective.stream().anyMatch(rp ->
                    "fees.reminder_history.view".equals(rp.getPermission().getPermissionKey())
                            && "school".equalsIgnoreCase(rp.getScopeType())
            );
        } catch (Exception ignored) {
            return false;
        }
    }

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

            List<com.school.erp.entity.finance.FeeDue> dues = feeDueRepository.findByStudentIdAndSchoolIdAndStatusInOrderByDueDateAsc(
                    studentId, schoolId, List.of("PENDING", "UNPAID", "PARTIAL", "PARTIALLY_PAID", "OVERDUE"));
            BigDecimal totalPending = dues.stream()
                    .map(d -> {
                        BigDecimal amt = d.getAmount() != null ? d.getAmount() : BigDecimal.ZERO;
                        BigDecimal paid = d.getPaidAmount() != null ? d.getPaidAmount() : BigDecimal.ZERO;
                        BigDecimal disc = d.getDiscountAmount() != null ? d.getDiscountAmount() : BigDecimal.ZERO;
                        return amt.subtract(paid).subtract(disc).max(BigDecimal.ZERO);
                    })
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            FeeReminder reminder = new FeeReminder();
            reminder.setSchool(school);
            reminder.setStaff(staff);
            reminder.setStudent(student);
            reminder.setFeeAmount(totalPending);
            reminder.setMethod(request.getMethod());
            reminder.setStatus("SENT");
            reminder.setSentAt(LocalDateTime.now());
            
            FeeReminder saved = feeReminderRepository.save(reminder);

            if (notificationService != null) {
                notificationService.notifyStudentAndParents(
                        schoolId,
                        student,
                        NotificationType.FEE_DUE_REMINDER,
                        NotificationPriority.HIGH,
                        "Fee Payment Reminder: " + student.getName(),
                        "Pending fee balance of " + totalPending + " is due for " + student.getName() + ". Please clear the dues at the earliest.",
                        "/finance/dues",
                        "Pay Now",
                        "FEE_REMINDER",
                        String.valueOf(saved.getId()),
                        Map.of(
                                "reminderId", saved.getId(),
                                "studentId", student.getId(),
                                "pendingAmount", totalPending
                        )
                );
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FeeReminderResponse> getReminderHistory(Long schoolId, Pageable pageable) {
        AuthenticatedUser currentUser = authContextService.requireCurrentUser();
        Staff staff = staffRepository.findByUserId(currentUser.userId())
                .orElse(null);

        boolean schoolScope = hasSchoolReminderScope(schoolId, currentUser);
        Page<FeeReminder> reminders;
        if (schoolScope || staff == null) {
            reminders = feeReminderRepository.findBySchoolIdOrderBySentAtDesc(schoolId, pageable);
        } else {
            reminders = feeReminderRepository.findBySchoolIdAndStaffIdOrderBySentAtDesc(schoolId, staff.getId(), pageable);
        }

        Map<Long, String> sectionNameMap = sectionRepository.findBySchoolId(schoolId).stream()
                .collect(Collectors.toMap(Section::getId, Section::getName, (a, b) -> a));

        return reminders.map(r -> {
            Student student = r.getStudent();
            String studentName = "";
            String className = null;
            String sectionName = null;
            if (student != null) {
                if (student.getFirstName() != null) {
                    studentName = student.getFirstName() + (student.getLastName() != null && !student.getLastName().isBlank() ? " " + student.getLastName() : "");
                } else if (student.getName() != null && !student.getName().isBlank()) {
                    studentName = student.getName();
                } else {
                    studentName = "Student " + (student.getAdmissionNo() != null ? student.getAdmissionNo() : student.getId());
                }
                if (student.getSchoolClass() != null) {
                    className = student.getSchoolClass().getName();
                }
                if (student.getSectionId() != null) {
                    sectionName = sectionNameMap.get(student.getSectionId());
                }
            }

            String sentBy = "School Admin";
            if (r.getStaff() != null) {
                String staffName = ((r.getStaff().getFirstName() != null ? r.getStaff().getFirstName() : "") + " " +
                        (r.getStaff().getLastName() != null ? r.getStaff().getLastName() : "")).trim();
                if (!staffName.isBlank()) {
                    sentBy = staffName;
                }
            }

            return FeeReminderResponse.builder()
                .id(r.getId())
                .studentId(student != null ? student.getId() : null)
                .studentName(studentName)
                .parentName(student != null && student.getGuardianName() != null ? student.getGuardianName() : "Guardian")
                .className(className)
                .sectionName(sectionName)
                .sentBy(sentBy)
                .feeAmount(r.getFeeAmount())
                .method(r.getMethod())
                .status(r.getStatus())
                .sentAt(r.getSentAt())
                .build();
        });
    }
}
