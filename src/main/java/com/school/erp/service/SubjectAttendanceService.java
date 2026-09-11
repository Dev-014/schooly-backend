package com.school.erp.service;

import com.school.erp.dto.attendance.BulkSubjectAttendanceRequest;
import com.school.erp.dto.attendance.StudentSubjectAttendanceSummaryDTO;
import com.school.erp.dto.attendance.SubjectAttendanceRequest;
import com.school.erp.dto.attendance.SubjectAttendanceResponse;
import com.school.erp.entity.*;
import com.school.erp.exception.ForbiddenException;
import com.school.erp.exception.ResourceNotFoundException;
import com.school.erp.repository.*;
import com.school.erp.security.AuthContextService;
import com.school.erp.security.AuthenticatedUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class SubjectAttendanceService {

    private final SubjectAttendanceRepository subjectAttendanceRepository;
    private final SchoolRepository schoolRepository;
    private final StudentRepository studentRepository;
    private final SchoolClassRepository classRepository;
    private final SectionRepository sectionRepository;
    private final SubjectRepository subjectRepository;
    private final TimetableEntryRepository timetableEntryRepository;
    private final ClassTeacherAssignmentRepository classTeacherAssignmentRepository;
    private final StaffRepository staffRepository;
    private final AuthContextService authContextService;

    public SubjectAttendanceService(
            SubjectAttendanceRepository subjectAttendanceRepository,
            SchoolRepository schoolRepository,
            StudentRepository studentRepository,
            SchoolClassRepository classRepository,
            SectionRepository sectionRepository,
            SubjectRepository subjectRepository,
            TimetableEntryRepository timetableEntryRepository,
            ClassTeacherAssignmentRepository classTeacherAssignmentRepository,
            StaffRepository staffRepository,
            AuthContextService authContextService
    ) {
        this.subjectAttendanceRepository = subjectAttendanceRepository;
        this.schoolRepository = schoolRepository;
        this.studentRepository = studentRepository;
        this.classRepository = classRepository;
        this.sectionRepository = sectionRepository;
        this.subjectRepository = subjectRepository;
        this.timetableEntryRepository = timetableEntryRepository;
        this.classTeacherAssignmentRepository = classTeacherAssignmentRepository;
        this.staffRepository = staffRepository;
        this.authContextService = authContextService;
    }

    public List<SubjectAttendanceResponse> getSubjectAttendanceByPeriod(
            Long schoolId, Long classId, Long sectionId, Long subjectId, LocalDate attendanceDate
    ) {
        Long effectiveSchoolId = authContextService.resolveSchoolId(schoolId);
        List<SubjectAttendance> records = (sectionId != null)
                ? subjectAttendanceRepository.findBySchoolIdAndSchoolClassIdAndSectionIdAndSubjectIdAndAttendanceDate(
                effectiveSchoolId, classId, sectionId, subjectId, attendanceDate)
                : subjectAttendanceRepository.findBySchoolIdAndSchoolClassIdAndSubjectIdAndAttendanceDate(
                effectiveSchoolId, classId, subjectId, attendanceDate);


        return records.stream().map(this::toResponse).toList();
    }

    public List<SubjectAttendanceResponse> getStudentSubjectAttendance(Long schoolId, Long studentId) {
        Long effectiveSchoolId = authContextService.resolveSchoolId(schoolId);
        Long resolvedStudentId = studentId;

        if (resolvedStudentId == null) {
            AuthenticatedUser currentUser = authContextService.getCurrentUserOrNull();
            if (currentUser != null && currentUser.userId() != null) {
                List<Student> students = studentRepository.findByUserId(currentUser.userId());
                if (!students.isEmpty()) {
                    resolvedStudentId = students.get(0).getId();
                }
            }
        }

        if (resolvedStudentId == null) {
            return List.of();
        }

        return subjectAttendanceRepository.findBySchoolIdAndStudentId(effectiveSchoolId, resolvedStudentId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<StudentSubjectAttendanceSummaryDTO> getStudentSubjectSummary(Long schoolId, Long studentId) {
        Long effectiveSchoolId = authContextService.resolveSchoolId(schoolId);
        Long resolvedStudentId = studentId;

        if (resolvedStudentId == null) {
            AuthenticatedUser currentUser = authContextService.getCurrentUserOrNull();
            if (currentUser != null && currentUser.userId() != null) {
                List<Student> students = studentRepository.findByUserId(currentUser.userId());
                if (!students.isEmpty()) {
                    resolvedStudentId = students.get(0).getId();
                }
            }
        }

        if (resolvedStudentId == null) {
            return List.of();
        }

        List<Object[]> rawList = subjectAttendanceRepository.getSubjectWiseAttendanceSummary(effectiveSchoolId, resolvedStudentId);
        return rawList.stream().map(row -> {
            Long subId = ((Number) row[0]).longValue();
            String subName = (String) row[1];
            String subCode = (String) row[2];
            long total = ((Number) row[3]).longValue();
            long attended = ((Number) row[4]).longValue();
            int percentage = total == 0 ? 0 : (int) Math.round(((double) attended / total) * 100);
            return new StudentSubjectAttendanceSummaryDTO(subId, subName, subCode, total, attended, percentage);
        }).toList();
    }

    @Transactional
    public List<SubjectAttendanceResponse> saveBulkSubjectAttendance(BulkSubjectAttendanceRequest request) {
        Long effectiveSchoolId = authContextService.resolveSchoolId(request.schoolId());
        School school = schoolRepository.findById(effectiveSchoolId)
                .orElseThrow(() -> new ResourceNotFoundException("School not found"));

        SchoolClass schoolClass = classRepository.findByIdAndSchoolId(request.classId(), effectiveSchoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Class not found"));

        Section section = (request.sectionId() != null)
                ? sectionRepository.findByIdAndSchoolId(request.sectionId(), effectiveSchoolId).orElse(null)
                : null;

        Subject subject = subjectRepository.findByIdAndSchoolId(request.subjectId(), effectiveSchoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found"));

        TimetableEntry timetableEntry = (request.timetableEntryId() != null)
                ? timetableEntryRepository.findById(request.timetableEntryId()).orElse(null)
                : null;

        validateSubjectTeacherOrAdmin(effectiveSchoolId, request.classId(), request.sectionId(), request.subjectId());

        Long markedBy = resolveCurrentStaffOrUserId();

        List<SubjectAttendanceResponse> responses = new ArrayList<>();

        for (BulkSubjectAttendanceRequest.SubjectAttendanceEntry entry : request.entries()) {
            Student student = studentRepository.findByIdAndSchoolId(entry.studentId(), effectiveSchoolId)
                    .orElseThrow(() -> new ResourceNotFoundException("Student not found for id: " + entry.studentId()));

            SubjectAttendance attendance = (request.timetableEntryId() != null)
                    ? subjectAttendanceRepository.findBySchoolIdAndStudentIdAndSubjectIdAndAttendanceDateAndTimetableEntryId(
                            effectiveSchoolId, student.getId(), subject.getId(), request.attendanceDate(), request.timetableEntryId())
                    .orElse(new SubjectAttendance())
                    : new SubjectAttendance();

            attendance.setSchool(school);
            attendance.setStudent(student);
            attendance.setSchoolClass(schoolClass);
            attendance.setSection(section);
            attendance.setSubject(subject);
            attendance.setTimetableEntry(timetableEntry);
            attendance.setAttendanceDate(request.attendanceDate());
            attendance.setStatus(entry.status());
            attendance.setRemarks(entry.remarks());
            attendance.setMarkedBy(markedBy);

            responses.add(toResponse(subjectAttendanceRepository.save(attendance)));
        }

        return responses;
    }

    @Transactional
    public SubjectAttendanceResponse createSubjectAttendance(SubjectAttendanceRequest request) {
        Long effectiveSchoolId = authContextService.resolveSchoolId(request.schoolId());
        validateSubjectTeacherOrAdmin(effectiveSchoolId, request.classId(), request.sectionId(), request.subjectId());

        School school = schoolRepository.findById(effectiveSchoolId)
                .orElseThrow(() -> new ResourceNotFoundException("School not found"));
        Student student = studentRepository.findByIdAndSchoolId(request.studentId(), effectiveSchoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        SchoolClass schoolClass = classRepository.findByIdAndSchoolId(request.classId(), effectiveSchoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Class not found"));
        Section section = (request.sectionId() != null)
                ? sectionRepository.findByIdAndSchoolId(request.sectionId(), effectiveSchoolId).orElse(null)
                : null;
        Subject subject = subjectRepository.findByIdAndSchoolId(request.subjectId(), effectiveSchoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found"));
        TimetableEntry timetableEntry = (request.timetableEntryId() != null)
                ? timetableEntryRepository.findById(request.timetableEntryId()).orElse(null)
                : null;

        SubjectAttendance attendance = new SubjectAttendance();
        attendance.setSchool(school);
        attendance.setStudent(student);
        attendance.setSchoolClass(schoolClass);
        attendance.setSection(section);
        attendance.setSubject(subject);
        attendance.setTimetableEntry(timetableEntry);
        attendance.setAttendanceDate(request.attendanceDate());
        attendance.setStatus(request.status());
        attendance.setRemarks(request.remarks());
        attendance.setMarkedBy(resolveCurrentStaffOrUserId());

        return toResponse(subjectAttendanceRepository.save(attendance));
    }

    private void validateSubjectTeacherOrAdmin(Long schoolId, Long classId, Long sectionId, Long subjectId) {
        AuthenticatedUser currentUser = authContextService.getCurrentUserOrNull();
        if (currentUser == null) {
            return;
        }
        if (currentUser.role() == UserRole.SUPER_ADMIN || currentUser.role() == UserRole.ADMIN) {
            return;
        }

        if (currentUser.userId() != null) {
            Optional<Staff> staffOpt = staffRepository.findByUserId(currentUser.userId());
            if (staffOpt.isEmpty()) {
                throw new ForbiddenException("Staff profile not found. Only the assigned Subject Teacher, Class Teacher, or Administrator can mark subject attendance.");
            }
            Long staffId = staffOpt.get().getId();

            // 1. Check if assigned as Subject Teacher in Timetable
            boolean isSubjectTeacher;
            if (sectionId != null) {
                isSubjectTeacher = timetableEntryRepository.existsBySchoolIdAndTeacherIdAndSchoolClassIdAndSectionIdAndSubjectId(
                        schoolId, staffId, classId, sectionId, subjectId);
            } else {
                isSubjectTeacher = timetableEntryRepository.existsBySchoolIdAndTeacherIdAndSchoolClassIdAndSubjectId(
                        schoolId, staffId, classId, subjectId);
            }

            if (isSubjectTeacher) {
                return;
            }

            // 2. Check if assigned as Class Teacher for this class/section
            boolean isClassTeacher;
            if (sectionId != null) {
                isClassTeacher = classTeacherAssignmentRepository.existsBySchoolIdAndStaffIdAndSchoolClassIdAndSectionIdAndStatus(
                        schoolId, staffId, classId, sectionId, "ACTIVE");
            } else {
                isClassTeacher = classTeacherAssignmentRepository.existsBySchoolIdAndStaffIdAndSchoolClassIdAndStatus(
                        schoolId, staffId, classId, "ACTIVE");
            }

            if (isClassTeacher) {
                return;
            }

            throw new ForbiddenException("Access Denied: Only the assigned Subject Teacher, Class Teacher, or School Administrator can mark attendance for this subject.");
        }
    }

    private Long resolveCurrentStaffOrUserId() {
        AuthenticatedUser currentUser = authContextService.getCurrentUserOrNull();
        if (currentUser == null) {
            return null;
        }
        if (currentUser.userId() != null) {
            return staffRepository.findByUserId(currentUser.userId())
                    .map(Staff::getId)
                    .orElse(currentUser.userId());
        }
        return null;
    }

    private SubjectAttendanceResponse toResponse(SubjectAttendance sa) {
        String studentName = sa.getStudent().getName() != null
                ? sa.getStudent().getName()
                : ((sa.getStudent().getFirstName() != null ? sa.getStudent().getFirstName() : "") + " " +
                   (sa.getStudent().getLastName() != null ? sa.getStudent().getLastName() : "")).trim();

        return new SubjectAttendanceResponse(
                sa.getId(),
                sa.getSchool().getId(),
                sa.getStudent().getId(),
                studentName,
                sa.getSchoolClass().getId(),
                sa.getSchoolClass().getName(),
                sa.getSection() != null ? sa.getSection().getId() : null,
                sa.getSection() != null ? sa.getSection().getName() : null,
                sa.getSubject().getId(),
                sa.getSubject().getName(),
                sa.getSubject().getCode(),
                sa.getTimetableEntry() != null ? sa.getTimetableEntry().getId() : null,
                sa.getAttendanceDate(),
                sa.getStatus(),
                sa.getRemarks()
        );
    }

}
