package com.school.erp.service.attendance;

import com.school.erp.dto.attendance.AttendanceRequest;
import com.school.erp.dto.attendance.AttendanceResponse;
import com.school.erp.dto.attendance.BulkAttendanceRequest;
import com.school.erp.entity.attendance.Attendance;
import com.school.erp.entity.superadmin.School;
import com.school.erp.entity.student.Student;
import com.school.erp.exception.ResourceNotFoundException;
import com.school.erp.repository.attendance.AttendanceRepository;
import com.school.erp.repository.superadmin.SchoolRepository;
import com.school.erp.repository.student.StudentRepository;
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
    private final com.school.erp.repository.academic.SchoolClassRepository schoolClassRepository;
    private final com.school.erp.repository.student.StudentLeaveRepository studentLeaveRepository;
    private final com.school.erp.repository.hr.StaffRepository staffRepository;
    private final com.school.erp.repository.academic.ClassTeacherAssignmentRepository assignmentRepository;
    private final com.school.erp.repository.auth.UserAssignmentRepository userAssignmentRepository;
    private final AuthContextService authContextService;

    public AttendanceService(
            AttendanceRepository attendanceRepository,
            SchoolRepository schoolRepository,
            StudentRepository studentRepository,
            com.school.erp.repository.academic.SchoolClassRepository schoolClassRepository,
            com.school.erp.repository.student.StudentLeaveRepository studentLeaveRepository,
            com.school.erp.repository.hr.StaffRepository staffRepository,
            com.school.erp.repository.academic.ClassTeacherAssignmentRepository assignmentRepository,
            com.school.erp.repository.auth.UserAssignmentRepository userAssignmentRepository,
            AuthContextService authContextService
    ) {
        this.attendanceRepository = attendanceRepository;
        this.schoolRepository = schoolRepository;
        this.studentRepository = studentRepository;
        this.schoolClassRepository = schoolClassRepository;
        this.studentLeaveRepository = studentLeaveRepository;
        this.staffRepository = staffRepository;
        this.assignmentRepository = assignmentRepository;
        this.userAssignmentRepository = userAssignmentRepository;
        this.authContextService = authContextService;
    }


    public List<AttendanceResponse> getAttendance(Long schoolId, Long studentId) {
        Long effectiveSchoolId = authContextService.resolveSchoolId(schoolId);
        Long resolvedStudentId = studentId;
        if (resolvedStudentId == null) {
            com.school.erp.security.AuthenticatedUser currentUser = authContextService.getCurrentUserOrNull();
            if (currentUser != null && currentUser.userId() != null) {
                List<Student> students = studentRepository.findByUserId(currentUser.userId());
                if (!students.isEmpty()) {
                    resolvedStudentId = students.get(0).getId();
                }
            }
        }
        List<Attendance> records = resolvedStudentId == null
                ? attendanceRepository.findBySchoolId(effectiveSchoolId)
                : attendanceRepository.findBySchoolIdAndStudentId(effectiveSchoolId, resolvedStudentId);
        return records.stream().map(this::toResponse).toList();
    }

    public com.school.erp.dto.attendance.AttendanceSummaryDTO getSummaryToday(Long schoolId) {
        return getSummaryToday(schoolId, null);
    }

    public com.school.erp.dto.attendance.AttendanceSummaryDTO getSummaryToday(Long schoolId, java.time.LocalDate targetDate) {
        Long effectiveSchoolId = authContextService.resolveSchoolId(schoolId);
        java.time.LocalDate date = targetDate != null ? targetDate : java.time.LocalDate.now();
        
        long totalStudents = studentRepository.countBySchoolId(effectiveSchoolId);
        long present = attendanceRepository.countBySchoolIdAndAttendanceDateAndStatus(effectiveSchoolId, date, "PRESENT");
        long absent = attendanceRepository.countBySchoolIdAndAttendanceDateAndStatus(effectiveSchoolId, date, "ABSENT");
        long late = attendanceRepository.countBySchoolIdAndAttendanceDateAndStatus(effectiveSchoolId, date, "LATE");
        long excused = attendanceRepository.countBySchoolIdAndAttendanceDateAndStatus(effectiveSchoolId, date, "EXCUSED");
        
        long pendingLeaves = studentLeaveRepository.countBySchoolIdAndStatus(effectiveSchoolId, "PENDING");

        long onCampus = present + late;
        int presentPercent = totalStudents == 0 ? 0 : (int) Math.round(((double) onCampus / totalStudents) * 100);
        
        return new com.school.erp.dto.attendance.AttendanceSummaryDTO((int) totalStudents, (int) present, (int) absent, (int) late, presentPercent, (int) pendingLeaves);
    }

    public List<com.school.erp.dto.attendance.analytics.AttendanceTrendDTO> getAttendanceTrends(Long schoolId, int days) {
        return getAttendanceTrends(schoolId, days, null);
    }

    public List<com.school.erp.dto.attendance.analytics.AttendanceTrendDTO> getAttendanceTrends(Long schoolId, int days, Long classId) {
        Long effectiveSchoolId = authContextService.resolveSchoolId(schoolId);
        java.time.LocalDate startDate = java.time.LocalDate.now().minusDays(Math.max(1, days));
        List<Object[]> rawData = classId != null
                ? attendanceRepository.getAttendanceTrendsByDateAndClass(effectiveSchoolId, startDate, classId)
                : attendanceRepository.getAttendanceTrendsByDate(effectiveSchoolId, startDate);

        return rawData.stream().map(row -> new com.school.erp.dto.attendance.analytics.AttendanceTrendDTO(
                (java.time.LocalDate) row[0],
                ((Number) row[1]).intValue() * 100 / Math.max(1, ((Number) row[2]).intValue())
        )).toList();
    }

    public List<com.school.erp.dto.attendance.analytics.GradeAttendanceDTO> getGradeWiseAttendance(Long schoolId) {
        Long effectiveSchoolId = authContextService.resolveSchoolId(schoolId);
        List<Object[]> rawData = attendanceRepository.getGradeWiseAttendance(effectiveSchoolId);
        java.util.Map<String, com.school.erp.dto.attendance.analytics.GradeAttendanceDTO> resultMap = new java.util.LinkedHashMap<>();

        for (Object[] row : rawData) {
            String className = (String) row[0];
            int total = ((Number) row[4]).intValue();
            double avgAttendance = total == 0 ? 0 : ((Number) row[2]).doubleValue() * 100.0 / total;
            double lateFreq = total == 0 ? 0 : ((Number) row[3]).doubleValue() * 100.0 / total;
            String performance = avgAttendance >= 90 ? "EXCELLENT" : (avgAttendance >= 80 ? "GOOD" : "NEEDS_IMPROVEMENT");
            resultMap.put(className, new com.school.erp.dto.attendance.analytics.GradeAttendanceDTO(
                    className,
                    ((Number) row[1]).intValue(),
                    Math.round(avgAttendance * 10.0) / 10.0,
                    Math.round(lateFreq * 10.0) / 10.0,
                    performance
            ));
        }

        // Ensure all registered classes for the school appear in analytics
        List<com.school.erp.entity.academic.SchoolClass> allClasses = schoolClassRepository.findBySchoolId(effectiveSchoolId);
        for (com.school.erp.entity.academic.SchoolClass sc : allClasses) {
            if (!resultMap.containsKey(sc.getName())) {
                long classStudentCount = studentRepository.findBySchoolIdAndSchoolClassId(effectiveSchoolId, sc.getId()).size();
                resultMap.put(sc.getName(), new com.school.erp.dto.attendance.analytics.GradeAttendanceDTO(
                        sc.getName(),
                        (int) classStudentCount,
                        0.0,
                        0.0,
                        "NEEDS_IMPROVEMENT"
                ));
            }
        }

        return new java.util.ArrayList<>(resultMap.values());
    }

    public List<AttendanceResponse> getAttendanceByDate(Long schoolId, Long classId, Long sectionId, java.time.LocalDate attendanceDate) {
        Long effectiveSchoolId = authContextService.resolveSchoolId(schoolId);
        
        List<Student> students = (sectionId != null) 
            ? studentRepository.findBySchoolIdAndSchoolClassIdAndSectionId(effectiveSchoolId, classId, sectionId)
            : studentRepository.findBySchoolIdAndSchoolClassId(effectiveSchoolId, classId);
            
        List<Long> studentIds = students.stream().map(Student::getId).toList();
        
        if (studentIds.isEmpty()) return List.of();
        
        List<Attendance> records = attendanceRepository.findBySchoolIdAndAttendanceDateAndStudentIdIn(effectiveSchoolId, attendanceDate, studentIds);
        return records.stream().map(this::toResponse).toList();
    }

    @Transactional
    public List<AttendanceResponse> saveBulkAttendance(BulkAttendanceRequest request) {
        Long effectiveSchoolId = authContextService.resolveSchoolId(request.schoolId());
        School school = getSchool(effectiveSchoolId);
        
        List<AttendanceResponse> responses = new java.util.ArrayList<>();
        boolean validatedAuth = false;
        
        for (BulkAttendanceRequest.AttendanceEntry entry : request.entries()) {
            Student student = getStudent(entry.studentId(), effectiveSchoolId);
            
            if (!validatedAuth) {
                validateClassTeacherOrAdmin(effectiveSchoolId, student);
                validatedAuth = true;
            }
            
            Attendance attendance = attendanceRepository
                .findBySchoolIdAndAttendanceDateAndStudentId(effectiveSchoolId, request.attendanceDate(), student.getId())
                .orElse(new Attendance());
                
            attendance.setSchool(school);
            attendance.setStudent(student);
            attendance.setAttendanceDate(request.attendanceDate());
            attendance.setStatus(entry.status());
            
            responses.add(toResponse(attendanceRepository.save(attendance)));
        }
        
        return responses;
    }

    @Transactional
    public AttendanceResponse createAttendance(AttendanceRequest request) {
        Long effectiveSchoolId = authContextService.resolveSchoolId(request.schoolId());
        School school = getSchool(effectiveSchoolId);
        Student student = getStudent(request.studentId(), effectiveSchoolId);
        validateClassTeacherOrAdmin(effectiveSchoolId, student);

        Attendance attendance = new Attendance();
        attendance.setSchool(school);
        attendance.setStudent(student);
        attendance.setAttendanceDate(request.attendanceDate());
        attendance.setStatus(request.status());

        return toResponse(attendanceRepository.save(attendance));
    }

    private void validateClassTeacherOrAdmin(Long schoolId, Student student) {
        com.school.erp.security.AuthenticatedUser currentUser = authContextService.getCurrentUserOrNull();
        if (currentUser == null) {
            return;
        }
        // Super Admins and Admins can always mark attendance
        if (currentUser.role() == com.school.erp.entity.auth.UserRole.SUPER_ADMIN ||
            currentUser.role() == com.school.erp.entity.auth.UserRole.ADMIN) {
            return;
        }

        // For non-admin (e.g. TEACHER, STAFF), verify assignment
        if (currentUser.userId() != null) {
            Long classId = student.getSchoolClass() != null ? student.getSchoolClass().getId() : null;
            Long sectionId = student.getSectionId();

            if (classId != null) {
                // Check 1: class_teacher_assignments table
                boolean isAssignedInTable = false;
                java.util.Optional<com.school.erp.entity.hr.Staff> staffOpt = staffRepository.findByUserId(currentUser.userId());
                if (staffOpt.isPresent()) {
                    Long staffId = staffOpt.get().getId();
                    if (sectionId != null) {
                        isAssignedInTable = assignmentRepository.existsBySchoolIdAndStaffIdAndSchoolClassIdAndSectionIdAndStatus(
                                schoolId, staffId, classId, sectionId, "ACTIVE");
                    } else {
                        isAssignedInTable = assignmentRepository.existsBySchoolIdAndStaffIdAndSchoolClassIdAndStatus(
                                schoolId, staffId, classId, "ACTIVE");
                    }
                }

                // Check 2: user_assignments table (HR / Staff assignments)
                boolean isAssignedInUserAssignments;
                if (sectionId != null) {
                    isAssignedInUserAssignments = userAssignmentRepository.existsBySchoolIdAndUserIdAndAssignmentTypeAndClassIdAndSectionIdAndIsActiveTrue(
                            schoolId, currentUser.userId(), "class_teacher", classId, sectionId) ||
                            userAssignmentRepository.existsBySchoolIdAndUserIdAndAssignmentTypeAndClassIdAndSectionIdIsNullAndIsActiveTrue(
                                    schoolId, currentUser.userId(), "class_teacher", classId);
                } else {
                    isAssignedInUserAssignments = userAssignmentRepository.existsBySchoolIdAndUserIdAndAssignmentTypeAndClassIdAndIsActiveTrue(
                            schoolId, currentUser.userId(), "class_teacher", classId);
                }

                if (isAssignedInTable || isAssignedInUserAssignments) {
                    return;
                }

                throw new com.school.erp.exception.ForbiddenException(
                        "Access Denied: Only the assigned Class Teacher or School Administrator can mark daily roll call attendance for this class/section."
                );
            }
        }
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
