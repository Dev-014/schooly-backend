package com.school.erp.service;

import com.school.erp.dto.attendance.AttendanceRequest;
import com.school.erp.dto.attendance.AttendanceResponse;
import com.school.erp.dto.attendance.BulkAttendanceRequest;
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
    private final com.school.erp.repository.StudentLeaveRepository studentLeaveRepository;
    private final com.school.erp.repository.StaffRepository staffRepository;
    private final com.school.erp.repository.ClassTeacherAssignmentRepository assignmentRepository;
    private final AuthContextService authContextService;

    public AttendanceService(
            AttendanceRepository attendanceRepository,
            SchoolRepository schoolRepository,
            StudentRepository studentRepository,
            com.school.erp.repository.StudentLeaveRepository studentLeaveRepository,
            com.school.erp.repository.StaffRepository staffRepository,
            com.school.erp.repository.ClassTeacherAssignmentRepository assignmentRepository,
            AuthContextService authContextService
    ) {
        this.attendanceRepository = attendanceRepository;
        this.schoolRepository = schoolRepository;
        this.studentRepository = studentRepository;
        this.studentLeaveRepository = studentLeaveRepository;
        this.staffRepository = staffRepository;
        this.assignmentRepository = assignmentRepository;
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
        Long effectiveSchoolId = authContextService.resolveSchoolId(schoolId);
        java.time.LocalDate today = java.time.LocalDate.now();
        
        long totalStudents = studentRepository.countBySchoolId(effectiveSchoolId);
        long present = attendanceRepository.countBySchoolIdAndAttendanceDateAndStatus(effectiveSchoolId, today, "PRESENT");
        long absent = attendanceRepository.countBySchoolIdAndAttendanceDateAndStatus(effectiveSchoolId, today, "ABSENT");
        long late = attendanceRepository.countBySchoolIdAndAttendanceDateAndStatus(effectiveSchoolId, today, "LATE");
        long excused = attendanceRepository.countBySchoolIdAndAttendanceDateAndStatus(effectiveSchoolId, today, "EXCUSED");
        
        long pendingLeaves = studentLeaveRepository.countBySchoolIdAndStatus(effectiveSchoolId, "PENDING");

        present += late; // late is often considered present, or we can keep it separate. The frontend adds present + late. Let's keep it separate as returned by count.
        int presentPercent = totalStudents == 0 ? 0 : (int) (((double) (present) / totalStudents) * 100);
        
        return new com.school.erp.dto.attendance.AttendanceSummaryDTO((int)totalStudents, (int)present, (int)absent, (int)late, presentPercent, (int)pendingLeaves);
    }

    public List<com.school.erp.dto.attendance.analytics.AttendanceTrendDTO> getAttendanceTrends(Long schoolId, int days) {
        Long effectiveSchoolId = authContextService.resolveSchoolId(schoolId);
        java.time.LocalDate startDate = java.time.LocalDate.now().minusDays(days);
        List<Object[]> rawData = attendanceRepository.getAttendanceTrendsByDate(effectiveSchoolId, startDate);
        return rawData.stream().map(row -> new com.school.erp.dto.attendance.analytics.AttendanceTrendDTO(
                (java.time.LocalDate) row[0],
                ((Number) row[1]).intValue() * 100 / Math.max(1, ((Number) row[2]).intValue())
        )).toList();
    }

    public List<com.school.erp.dto.attendance.analytics.GradeAttendanceDTO> getGradeWiseAttendance(Long schoolId) {
        Long effectiveSchoolId = authContextService.resolveSchoolId(schoolId);
        List<Object[]> rawData = attendanceRepository.getGradeWiseAttendance(effectiveSchoolId);
        return rawData.stream().map(row -> {
            int total = ((Number) row[4]).intValue();
            double avgAttendance = total == 0 ? 0 : ((Number) row[2]).doubleValue() * 100.0 / total;
            double lateFreq = total == 0 ? 0 : ((Number) row[3]).doubleValue() * 100.0 / total;
            String performance = avgAttendance >= 90 ? "EXCELLENT" : (avgAttendance >= 80 ? "GOOD" : "NEEDS_IMPROVEMENT");
            return new com.school.erp.dto.attendance.analytics.GradeAttendanceDTO(
                    (String) row[0],
                    ((Number) row[1]).intValue(), // proxy for capacity using DISTINCT student ids
                    Math.round(avgAttendance * 10.0) / 10.0,
                    Math.round(lateFreq * 10.0) / 10.0,
                    performance
            );
        }).toList();
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
        if (currentUser.role() == com.school.erp.entity.UserRole.SUPER_ADMIN ||
            currentUser.role() == com.school.erp.entity.UserRole.ADMIN) {
            return;
        }

        // For non-admin (e.g. TEACHER, STAFF), verify assignment
        if (currentUser.userId() != null) {
            java.util.Optional<com.school.erp.entity.Staff> staffOpt = staffRepository.findByUserId(currentUser.userId());
            if (staffOpt.isEmpty()) {
                throw new com.school.erp.exception.ForbiddenException("Staff profile not found. Only the assigned Class Teacher or Administrator can mark daily roll call attendance.");
            }
            Long staffId = staffOpt.get().getId();
            Long classId = student.getSchoolClass() != null ? student.getSchoolClass().getId() : null;
            Long sectionId = student.getSectionId();

            if (classId != null) {

                boolean isAssigned;
                if (sectionId != null) {
                    isAssigned = assignmentRepository.existsBySchoolIdAndStaffIdAndSchoolClassIdAndSectionIdAndStatus(
                            schoolId, staffId, classId, sectionId, "ACTIVE");
                } else {
                    isAssigned = assignmentRepository.existsBySchoolIdAndStaffIdAndSchoolClassIdAndStatus(
                            schoolId, staffId, classId, "ACTIVE");
                }

                if (!isAssigned) {
                    throw new com.school.erp.exception.ForbiddenException(
                            "Access Denied: Only the assigned Class Teacher or School Administrator can mark daily roll call attendance for this class/section."
                    );
                }
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
