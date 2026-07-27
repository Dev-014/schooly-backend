package com.school.erp.service;

import com.school.erp.dto.academic.*;
import com.school.erp.entity.*;
import com.school.erp.exception.ResourceNotFoundException;
import com.school.erp.repository.*;
import com.school.erp.security.AuthContextService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;

@Service
@Transactional(readOnly = true)
public class AcademicService {

    private final AcademicYearRepository academicYearRepository;
    private final SectionRepository sectionRepository;
    private final SubjectRepository subjectRepository;
    private final TimetablePeriodRepository periodRepository;
    private final SchoolRepository schoolRepository;
    private final SchoolClassRepository classRepository;
    private final AuthContextService authContextService;
    private final EntitlementService entitlementService;
    private final ClassTeacherAssignmentRepository assignmentRepository;
    private final StaffRepository staffRepository;
    private final com.school.erp.repository.UserRepository userRepository;
    private final ClassSubjectAssignmentRepository classSubjectAssignmentRepository;
    private final StudentSubjectEnrollmentRepository studentSubjectEnrollmentRepository;
    private final StudentRepository studentRepository;
    private final TimetableEntryRepository timetableEntryRepository;

    public AcademicService(
            AcademicYearRepository academicYearRepository,
            SectionRepository sectionRepository,
            SubjectRepository subjectRepository,
            TimetablePeriodRepository periodRepository,
            SchoolRepository schoolRepository,
            SchoolClassRepository classRepository,
            AuthContextService authContextService,
            EntitlementService entitlementService,
            ClassTeacherAssignmentRepository assignmentRepository,
            StaffRepository staffRepository,
            com.school.erp.repository.UserRepository userRepository,
            ClassSubjectAssignmentRepository classSubjectAssignmentRepository,
            StudentSubjectEnrollmentRepository studentSubjectEnrollmentRepository,
            StudentRepository studentRepository,
            TimetableEntryRepository timetableEntryRepository
    ) {
        this.academicYearRepository = academicYearRepository;
        this.sectionRepository = sectionRepository;
        this.subjectRepository = subjectRepository;
        this.periodRepository = periodRepository;
        this.schoolRepository = schoolRepository;
        this.classRepository = classRepository;
        this.authContextService = authContextService;
        this.entitlementService = entitlementService;
        this.assignmentRepository = assignmentRepository;
        this.staffRepository = staffRepository;
        this.userRepository = userRepository;
        this.classSubjectAssignmentRepository = classSubjectAssignmentRepository;
        this.studentSubjectEnrollmentRepository = studentSubjectEnrollmentRepository;
        this.studentRepository = studentRepository;
        this.timetableEntryRepository = timetableEntryRepository;
    }

    // --- Academic Years ---
    public List<AcademicYearResponse> getAcademicYears(Long schoolId) {
        Long effectiveSchoolId = authContextService.resolveSchoolId(schoolId);
        entitlementService.enforceModuleAccess(effectiveSchoolId, "ACADEMICS");
        return academicYearRepository.findBySchoolId(effectiveSchoolId).stream()
                .map(this::toYearResponse)
                .toList();
    }

    @Transactional
    public AcademicYearResponse createAcademicYear(AcademicYearRequest request) {
        Long effectiveSchoolId = authContextService.resolveSchoolId(request.schoolId());
        entitlementService.enforceModuleAccess(effectiveSchoolId, "ACADEMICS");
        entitlementService.enforceSchoolActive(effectiveSchoolId);

        School school = getSchool(effectiveSchoolId);
        AcademicYear year = new AcademicYear();
        year.setSchool(school);
        year.setName(request.name());
        year.setStartDate(request.startDate());
        year.setEndDate(request.endDate());
        year.setStatus(request.status() != null ? request.status() : "ACTIVE");
        return toYearResponse(academicYearRepository.save(year));
    }

    @Transactional
    public AcademicYearResponse activateAcademicYear(Long id, Long schoolId) {
        Long effectiveSchoolId = authContextService.resolveSchoolId(schoolId);
        entitlementService.enforceModuleAccess(effectiveSchoolId, "ACADEMICS");
        entitlementService.enforceSchoolActive(effectiveSchoolId);

        // Deactivate existing years and activate requested
        List<AcademicYear> all = academicYearRepository.findBySchoolId(effectiveSchoolId);
        for (AcademicYear ay : all) {
            if (ay.getId().equals(id)) {
                ay.setStatus("ACTIVE");
            } else if ("ACTIVE".equalsIgnoreCase(ay.getStatus())) {
                ay.setStatus("INACTIVE");
            }
        }
        AcademicYear target = academicYearRepository.findByIdAndSchoolId(id, effectiveSchoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Academic year not found"));
        return toYearResponse(target);
    }

    @Transactional
    public AcademicYearResponse updateAcademicYear(Long id, Long schoolId, AcademicYearRequest request) {
        Long effectiveSchoolId = authContextService.resolveSchoolId(schoolId != null ? schoolId : request.schoolId());
        entitlementService.enforceModuleAccess(effectiveSchoolId, "ACADEMICS");
        entitlementService.enforceSchoolActive(effectiveSchoolId);

        AcademicYear target = academicYearRepository.findByIdAndSchoolId(id, effectiveSchoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Academic year not found"));
        if (request.name() != null) target.setName(request.name());
        if (request.startDate() != null) target.setStartDate(request.startDate());
        if (request.endDate() != null) target.setEndDate(request.endDate());
        if (request.status() != null) target.setStatus(request.status());
        return toYearResponse(academicYearRepository.save(target));
    }

    @Transactional
    public void deleteAcademicYear(Long id, Long schoolId) {
        Long effectiveSchoolId = authContextService.resolveSchoolId(schoolId);
        entitlementService.enforceModuleAccess(effectiveSchoolId, "ACADEMICS");
        entitlementService.enforceSchoolActive(effectiveSchoolId);

        AcademicYear target = academicYearRepository.findByIdAndSchoolId(id, effectiveSchoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Academic year not found"));
        academicYearRepository.delete(target);
    }

    // --- Sections ---
    public List<SectionResponse> getSections(Long schoolId, Long classId) {
        Long effectiveSchoolId = authContextService.resolveSchoolId(schoolId);
        entitlementService.enforceModuleAccess(effectiveSchoolId, "ACADEMICS");
        List<Section> list = classId == null
                ? sectionRepository.findBySchoolId(effectiveSchoolId)
                : sectionRepository.findBySchoolIdAndSchoolClassId(effectiveSchoolId, classId);
        return list.stream().map(this::toSectionResponse).toList();
    }

    @Transactional
    public SectionResponse createSection(SectionRequest request) {
        Long effectiveSchoolId = authContextService.resolveSchoolId(request.schoolId());
        entitlementService.enforceModuleAccess(effectiveSchoolId, "ACADEMICS");
        entitlementService.enforceSchoolActive(effectiveSchoolId);

        School school = getSchool(effectiveSchoolId);
        SchoolClass schoolClass = classRepository.findByIdAndSchoolId(request.classId(), effectiveSchoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Class not found"));
        Section sec = new Section();
        sec.setSchool(school);
        sec.setSchoolClass(schoolClass);
        sec.setName(request.name());
        sec.setRoomNumber(request.roomNumber());
        sec.setCapacity(request.capacity() != null ? request.capacity() : 40);
        return toSectionResponse(sectionRepository.save(sec));
    }

    @Transactional
    public SectionResponse updateSection(Long id, Long schoolId, SectionRequest request) {
        Long effectiveSchoolId = authContextService.resolveSchoolId(schoolId != null ? schoolId : request.schoolId());
        entitlementService.enforceModuleAccess(effectiveSchoolId, "ACADEMICS");
        entitlementService.enforceSchoolActive(effectiveSchoolId);

        Section sec = sectionRepository.findByIdAndSchoolId(id, effectiveSchoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Section not found"));
        if (request.classId() != null) {
            SchoolClass schoolClass = classRepository.findByIdAndSchoolId(request.classId(), effectiveSchoolId)
                    .orElseThrow(() -> new ResourceNotFoundException("Class not found"));
            sec.setSchoolClass(schoolClass);
        }
        sec.setName(request.name());
        sec.setRoomNumber(request.roomNumber());
        sec.setCapacity(request.capacity() != null ? request.capacity() : 40);
        return toSectionResponse(sectionRepository.save(sec));
    }

    @Transactional
    public void deleteSection(Long id, Long schoolId) {
        Long effectiveSchoolId = authContextService.resolveSchoolId(schoolId);
        entitlementService.enforceModuleAccess(effectiveSchoolId, "ACADEMICS");
        entitlementService.enforceSchoolActive(effectiveSchoolId);

        Section sec = sectionRepository.findByIdAndSchoolId(id, effectiveSchoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Section not found"));
        sectionRepository.delete(sec);
    }

    // --- Subjects ---
    public List<SubjectResponse> getSubjects(Long schoolId, String gradeLevel) {
        Long effectiveSchoolId = authContextService.resolveSchoolId(schoolId);
        entitlementService.enforceModuleAccess(effectiveSchoolId, "ACADEMICS");
        List<Subject> list = gradeLevel == null
                ? subjectRepository.findBySchoolId(effectiveSchoolId)
                : subjectRepository.findBySchoolIdAndGradeLevel(effectiveSchoolId, gradeLevel);
        return list.stream().map(this::toSubjectResponse).toList();
    }

    @Transactional
    public SubjectResponse createSubject(SubjectRequest request) {
        Long effectiveSchoolId = authContextService.resolveSchoolId(request.schoolId());
        entitlementService.enforceModuleAccess(effectiveSchoolId, "ACADEMICS");
        entitlementService.enforceSchoolActive(effectiveSchoolId);

        School school = getSchool(effectiveSchoolId);
        Subject sub = new Subject();
        sub.setSchool(school);
        sub.setCode(generateSubjectCode(request.name(), effectiveSchoolId));
        sub.setName(request.name());
        sub.setType(request.type() != null ? request.type() : "THEORY");
        sub.setCredits(request.credits() != null ? request.credits() : 3);
        sub.setGradeLevel(request.gradeLevel());
        sub.setStatus("ACTIVE");
        return toSubjectResponse(subjectRepository.save(sub));
    }

    @Transactional
    public SubjectResponse updateSubject(Long id, Long schoolId, SubjectRequest request) {
        Long effectiveSchoolId = authContextService.resolveSchoolId(schoolId != null ? schoolId : request.schoolId());
        entitlementService.enforceModuleAccess(effectiveSchoolId, "ACADEMICS");
        entitlementService.enforceSchoolActive(effectiveSchoolId);

        Subject sub = subjectRepository.findByIdAndSchoolId(id, effectiveSchoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found"));
        // Code is never overwritten on update — it was auto-generated at creation
        sub.setName(request.name());
        sub.setType(request.type() != null ? request.type() : "THEORY");
        sub.setCredits(request.credits() != null ? request.credits() : 3);
        sub.setGradeLevel(request.gradeLevel());
        return toSubjectResponse(subjectRepository.save(sub));
    }

    @Transactional
    public void deleteSubject(Long id, Long schoolId) {
        Long effectiveSchoolId = authContextService.resolveSchoolId(schoolId);
        entitlementService.enforceModuleAccess(effectiveSchoolId, "ACADEMICS");
        entitlementService.enforceSchoolActive(effectiveSchoolId);

        Subject sub = subjectRepository.findByIdAndSchoolId(id, effectiveSchoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found"));
        subjectRepository.delete(sub);
    }

    // --- Timetable Periods ---
    public List<TimetablePeriodResponse> getTimetablePeriods(Long schoolId) {
        Long effectiveSchoolId = authContextService.resolveSchoolId(schoolId);
        entitlementService.enforceModuleAccess(effectiveSchoolId, "ACADEMICS");
        return periodRepository.findBySchoolIdOrderByPeriodNumberAsc(effectiveSchoolId).stream()
                .map(this::toPeriodResponse)
                .toList();
    }

    @Transactional
    public TimetablePeriodResponse createTimetablePeriod(TimetablePeriodRequest request) {
        Long effectiveSchoolId = authContextService.resolveSchoolId(request.schoolId());
        entitlementService.enforceModuleAccess(effectiveSchoolId, "ACADEMICS");
        entitlementService.enforceSchoolActive(effectiveSchoolId);

        School school = getSchool(effectiveSchoolId);
        TimetablePeriod p = new TimetablePeriod();
        p.setSchool(school);
        p.setPeriodNumber(request.periodNumber());
        p.setName(request.name());
        p.setStartTime(request.startTime());
        p.setEndTime(request.endTime());
        p.setIsBreak(request.isBreak() != null && request.isBreak());
        return toPeriodResponse(periodRepository.save(p));
    }

    @Transactional
    public TimetablePeriodResponse updateTimetablePeriod(Long id, Long schoolId, TimetablePeriodRequest request) {
        Long effectiveSchoolId = authContextService.resolveSchoolId(schoolId != null ? schoolId : request.schoolId());
        entitlementService.enforceModuleAccess(effectiveSchoolId, "ACADEMICS");
        entitlementService.enforceSchoolActive(effectiveSchoolId);

        TimetablePeriod p = periodRepository.findByIdAndSchoolId(id, effectiveSchoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Timetable period not found"));
        if (request.periodNumber() != null) p.setPeriodNumber(request.periodNumber());
        if (request.name() != null) p.setName(request.name());
        if (request.startTime() != null) p.setStartTime(request.startTime());
        if (request.endTime() != null) p.setEndTime(request.endTime());
        if (request.isBreak() != null) p.setIsBreak(request.isBreak());
        return toPeriodResponse(periodRepository.save(p));
    }

    @Transactional
    public void deleteTimetablePeriod(Long id, Long schoolId) {
        Long effectiveSchoolId = authContextService.resolveSchoolId(schoolId);
        entitlementService.enforceModuleAccess(effectiveSchoolId, "ACADEMICS");
        entitlementService.enforceSchoolActive(effectiveSchoolId);

        TimetablePeriod p = periodRepository.findByIdAndSchoolId(id, effectiveSchoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Timetable period not found"));
        periodRepository.delete(p);
    }

    // --- Class Teacher Assignments ---
    public List<ClassTeacherAssignmentResponse> getClassTeacherAssignments(Long schoolId, Long academicYearId) {
        Long effectiveSchoolId = authContextService.resolveSchoolId(schoolId);
        entitlementService.enforceModuleAccess(effectiveSchoolId, "ACADEMICS");
        
        List<ClassTeacherAssignment> list;
        if (academicYearId != null) {
            list = assignmentRepository.findBySchoolIdAndAcademicYearId(effectiveSchoolId, academicYearId);
        } else {
            // Find active year
            AcademicYear activeYear = academicYearRepository.findBySchoolId(effectiveSchoolId).stream()
                    .filter(y -> "ACTIVE".equals(y.getStatus()))
                    .findFirst()
                    .orElse(null);
            if (activeYear != null) {
                list = assignmentRepository.findBySchoolIdAndAcademicYearId(effectiveSchoolId, activeYear.getId());
            } else {
                list = List.of();
            }
        }
        return list.stream().map(this::toAssignmentResponse).toList();
    }

    @Transactional
    public ClassTeacherAssignmentResponse assignClassTeacher(ClassTeacherAssignmentRequest request) {
        Long effectiveSchoolId = authContextService.resolveSchoolId(request.schoolId());
        entitlementService.enforceModuleAccess(effectiveSchoolId, "ACADEMICS");
        entitlementService.enforceSchoolActive(effectiveSchoolId);

        School school = getSchool(effectiveSchoolId);
        Staff staff = staffRepository.findByIdAndSchoolId(request.staffId(), effectiveSchoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found"));
        SchoolClass schoolClass = classRepository.findByIdAndSchoolId(request.classId(), effectiveSchoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Class not found"));
        Section section = sectionRepository.findByIdAndSchoolId(request.sectionId(), effectiveSchoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Section not found"));
        AcademicYear year = academicYearRepository.findByIdAndSchoolId(request.academicYearId(), effectiveSchoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Academic Year not found"));

        // Enforce policy: deactivate previous active assignment for this section and year
        assignmentRepository.findBySchoolIdAndSectionIdAndAcademicYearIdAndStatus(effectiveSchoolId, section.getId(), year.getId(), "ACTIVE")
                .ifPresent(existing -> {
                    existing.setStatus("INACTIVE");
                    assignmentRepository.saveAndFlush(existing);
                });

        ClassTeacherAssignment assignment = new ClassTeacherAssignment();
        assignment.setSchool(school);
        assignment.setStaff(staff);
        assignment.setSchoolClass(schoolClass);
        assignment.setSection(section);
        assignment.setAcademicYear(year);
        assignment.setStatus(request.status() != null ? request.status() : "ACTIVE");

        return toAssignmentResponse(assignmentRepository.save(assignment));
    }

    @Transactional
    public ClassTeacherAssignmentResponse updateAssignmentStatus(Long id, Long schoolId, String status) {
        Long effectiveSchoolId = authContextService.resolveSchoolId(schoolId);
        entitlementService.enforceModuleAccess(effectiveSchoolId, "ACADEMICS");
        entitlementService.enforceSchoolActive(effectiveSchoolId);

        ClassTeacherAssignment assignment = assignmentRepository.findByIdAndSchoolId(id, effectiveSchoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found"));
        assignment.setStatus(status);
        return toAssignmentResponse(assignmentRepository.save(assignment));
    }

    private School getSchool(Long schoolId) {
        return schoolRepository.findById(schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("School not found for id " + schoolId));
    }

    private AcademicYearResponse toYearResponse(AcademicYear ay) {
        return new AcademicYearResponse(ay.getId(), ay.getSchool().getId(), ay.getName(), ay.getStartDate(), ay.getEndDate(), ay.getStatus());
    }

    private SectionResponse toSectionResponse(Section sec) {
        return new SectionResponse(sec.getId(), sec.getSchoolClass().getId(), sec.getSchoolClass().getName(), sec.getSchool().getId(), sec.getName(), sec.getRoomNumber(), sec.getCapacity());
    }

    private SubjectResponse toSubjectResponse(Subject sub) {
        return new SubjectResponse(sub.getId(), sub.getSchool().getId(), sub.getCode(), sub.getName(), sub.getType(), sub.getCredits(), sub.getGradeLevel(), sub.getStatus() != null ? sub.getStatus() : "ACTIVE");
    }

    /**
     * Auto-generates a unique subject code using first 3 letters of the name (uppercased)
     * plus a 3-digit zero-padded sequence number scoped to the school.
     * Example: "Advanced Mathematics" → prefix "MAT", checks existing MAT-XXX codes,
     * returns next available e.g. "MAT-003".
     */
    private String generateSubjectCode(String name, Long schoolId) {
        // Derive prefix: strip non-alpha, take first 3 uppercase letters
        String cleaned = name.replaceAll("[^a-zA-Z]", "").toUpperCase();
        String rawPrefix = cleaned.length() >= 3 ? cleaned.substring(0, 3) : cleaned;
        final String prefix = rawPrefix.isEmpty() ? "SUB" : rawPrefix;

        List<Subject> existing = subjectRepository.findBySchoolIdAndCodeStartingWith(schoolId, prefix + "-");

        String pattern = prefix + "-\\d+";
        int nextNumber = existing.stream()
                .map(Subject::getCode)
                .filter(code -> code != null && code.matches(pattern))
                .map(code -> {
                    try { return Integer.parseInt(code.substring(prefix.length() + 1)); }
                    catch (NumberFormatException e) { return 0; }
                })
                .max(Comparator.naturalOrder())
                .orElse(0) + 1;

        return String.format("%s-%03d", prefix, nextNumber);
    }

    private TimetablePeriodResponse toPeriodResponse(TimetablePeriod p) {
        return new TimetablePeriodResponse(p.getId(), p.getSchool().getId(), p.getPeriodNumber(), p.getName(), p.getStartTime(), p.getEndTime(), p.getIsBreak());
    }

    private ClassTeacherAssignmentResponse toAssignmentResponse(ClassTeacherAssignment a) {
        String firstName = a.getStaff().getFirstName();
        String lastName = a.getStaff().getLastName();
        String email = a.getStaff().getEmail();

        if (a.getStaff().getUserId() != null && (firstName == null || lastName == null || email == null)) {
            User user = userRepository.findById(a.getStaff().getUserId()).orElse(null);
            if (user != null) {
                if (firstName == null || lastName == null) {
                    String[] parts = user.getName() != null ? user.getName().split(" ", 2) : new String[]{"Unknown", ""};
                    if (firstName == null) firstName = parts[0];
                    if (lastName == null) lastName = parts.length > 1 ? parts[1] : "";
                }
                if (email == null) email = user.getEmail();
            }
        }

        String staffName = (firstName != null ? firstName : "") + (lastName != null && !lastName.isEmpty() ? " " + lastName : "");

        return new ClassTeacherAssignmentResponse(
                a.getId(),
                a.getSchool().getId(),
                a.getStaff().getId(),
                staffName.trim(),
                email,
                a.getSchoolClass().getId(),
                a.getSchoolClass().getName(),
                a.getSection().getId(),
                a.getSection().getName(),
                a.getAcademicYear().getId(),
                a.getAcademicYear().getName(),
                a.getStatus()
        );
    }

    // =========================================================
    // --- Class Subject Assignments (Level 2) ---
    // =========================================================

    public List<ClassSubjectAssignmentResponse> getClassSubjectAssignments(Long schoolId, Long classId, Long sectionId, Long academicYearId) {
        Long effectiveSchoolId = authContextService.resolveSchoolId(schoolId);
        entitlementService.enforceModuleAccess(effectiveSchoolId, "ACADEMICS");

        Long effectiveYearId = resolveActiveYear(effectiveSchoolId, academicYearId);
        if (effectiveYearId == null) return List.of();

        List<ClassSubjectAssignment> list;
        if (sectionId != null) {
            list = classSubjectAssignmentRepository.findBySchoolIdAndSchoolClassIdAndSectionIdAndAcademicYearId(
                    effectiveSchoolId, classId, sectionId, effectiveYearId);
        } else if (classId != null) {
            list = classSubjectAssignmentRepository.findBySchoolIdAndSchoolClassIdAndAcademicYearId(
                    effectiveSchoolId, classId, effectiveYearId);
        } else {
            list = classSubjectAssignmentRepository.findBySchoolIdAndAcademicYearId(effectiveSchoolId, effectiveYearId);
        }
        return list.stream().map(this::toClassSubjectAssignmentResponse).toList();
    }

    @Transactional
    public List<ClassSubjectAssignmentResponse> assignSubjectsToClass(ClassSubjectAssignmentRequest request) {
        Long effectiveSchoolId = authContextService.resolveSchoolId(request.schoolId());
        entitlementService.enforceModuleAccess(effectiveSchoolId, "ACADEMICS");
        entitlementService.enforceSchoolActive(effectiveSchoolId);

        School school = getSchool(effectiveSchoolId);
        SchoolClass schoolClass = classRepository.findByIdAndSchoolId(request.classId(), effectiveSchoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Class not found"));
        Section section = request.sectionId() != null
                ? sectionRepository.findByIdAndSchoolId(request.sectionId(), effectiveSchoolId)
                        .orElseThrow(() -> new ResourceNotFoundException("Section not found"))
                : null;
        AcademicYear year = academicYearRepository.findByIdAndSchoolId(request.academicYearId(), effectiveSchoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Academic year not found"));

        return request.subjects().stream().map(entry -> {
            Subject subject = subjectRepository.findByIdAndSchoolId(entry.subjectId(), effectiveSchoolId)
                    .orElseThrow(() -> new ResourceNotFoundException("Subject not found: " + entry.subjectId()));

            // Upsert: find existing or create new
            Long sectionId = section != null ? section.getId() : null;
            ClassSubjectAssignment csa = classSubjectAssignmentRepository
                    .findBySchoolIdAndSchoolClassIdAndSectionIdAndSubjectIdAndAcademicYearId(
                            effectiveSchoolId, schoolClass.getId(), sectionId, subject.getId(), year.getId())
                    .orElse(new ClassSubjectAssignment());

            csa.setSchool(school);
            csa.setSchoolClass(schoolClass);
            csa.setSection(section);
            csa.setSubject(subject);
            csa.setAcademicYear(year);
            csa.setSubjectType(entry.subjectType() != null ? entry.subjectType() : "CORE");
            csa.setStatus("ACTIVE");

            return toClassSubjectAssignmentResponse(classSubjectAssignmentRepository.save(csa));
        }).toList();
    }

    @Transactional
    public void removeSubjectFromClass(Long id, Long schoolId) {
        Long effectiveSchoolId = authContextService.resolveSchoolId(schoolId);
        entitlementService.enforceModuleAccess(effectiveSchoolId, "ACADEMICS");
        entitlementService.enforceSchoolActive(effectiveSchoolId);
        ClassSubjectAssignment csa = classSubjectAssignmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found"));
        if (!csa.getSchool().getId().equals(effectiveSchoolId)) {
            throw new ResourceNotFoundException("Assignment not found");
        }
        classSubjectAssignmentRepository.delete(csa);
    }

    // =========================================================
    // --- Student Subject Enrollments (Level 3) ---
    // =========================================================

    public List<StudentSubjectEnrollmentResponse> getStudentEnrollments(Long schoolId, Long studentId, Long academicYearId) {
        Long effectiveSchoolId = authContextService.resolveSchoolId(schoolId);
        entitlementService.enforceModuleAccess(effectiveSchoolId, "ACADEMICS");

        Long effectiveYearId = resolveActiveYear(effectiveSchoolId, academicYearId);
        if (effectiveYearId == null) return List.of();

        List<StudentSubjectEnrollment> list = studentId != null
                ? studentSubjectEnrollmentRepository.findBySchoolIdAndStudentIdAndAcademicYearId(effectiveSchoolId, studentId, effectiveYearId)
                : studentSubjectEnrollmentRepository.findBySchoolIdAndAcademicYearId(effectiveSchoolId, effectiveYearId);
        return list.stream().map(this::toEnrollmentResponse).toList();
    }

    @Transactional
    public StudentSubjectEnrollmentResponse enrollStudentInSubject(StudentSubjectEnrollmentRequest request) {
        Long effectiveSchoolId = authContextService.resolveSchoolId(request.schoolId());
        entitlementService.enforceModuleAccess(effectiveSchoolId, "ACADEMICS");
        entitlementService.enforceSchoolActive(effectiveSchoolId);

        School school = getSchool(effectiveSchoolId);
        Student student = studentRepository.findByIdAndSchoolId(request.studentId(), effectiveSchoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        Subject subject = subjectRepository.findByIdAndSchoolId(request.subjectId(), effectiveSchoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found"));
        AcademicYear year = academicYearRepository.findByIdAndSchoolId(request.academicYearId(), effectiveSchoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Academic year not found"));

        // Upsert enrollment
        StudentSubjectEnrollment enrollment = studentSubjectEnrollmentRepository
                .findBySchoolIdAndStudentIdAndSubjectIdAndAcademicYearId(
                        effectiveSchoolId, student.getId(), subject.getId(), year.getId())
                .orElse(new StudentSubjectEnrollment());

        enrollment.setSchool(school);
        enrollment.setStudent(student);
        enrollment.setSubject(subject);
        enrollment.setAcademicYear(year);
        enrollment.setEnrollmentType(request.enrollmentType() != null ? request.enrollmentType() : "ELECTIVE");
        enrollment.setStatus("CONFIRMED");

        return toEnrollmentResponse(studentSubjectEnrollmentRepository.save(enrollment));
    }

    @Transactional
    public void removeStudentEnrollment(Long id, Long schoolId) {
        Long effectiveSchoolId = authContextService.resolveSchoolId(schoolId);
        entitlementService.enforceModuleAccess(effectiveSchoolId, "ACADEMICS");
        StudentSubjectEnrollment e = studentSubjectEnrollmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found"));
        if (!e.getSchool().getId().equals(effectiveSchoolId)) {
            throw new ResourceNotFoundException("Enrollment not found");
        }
        studentSubjectEnrollmentRepository.delete(e);
    }

    // =========================================================
    // --- Private Helpers ---
    // =========================================================

    private Long resolveActiveYear(Long schoolId, Long requestedYearId) {
        if (requestedYearId != null) return requestedYearId;
        return academicYearRepository.findBySchoolId(schoolId).stream()
                .filter(y -> "ACTIVE".equals(y.getStatus()))
                .findFirst()
                .map(AcademicYear::getId)
                .orElse(null);
    }

    private ClassSubjectAssignmentResponse toClassSubjectAssignmentResponse(ClassSubjectAssignment csa) {
        return new ClassSubjectAssignmentResponse(
                csa.getId(),
                csa.getSchool().getId(),
                csa.getSchoolClass().getId(),
                csa.getSchoolClass().getName(),
                csa.getSection() != null ? csa.getSection().getId() : null,
                csa.getSection() != null ? csa.getSection().getName() : null,
                csa.getSubject().getId(),
                csa.getSubject().getName(),
                csa.getSubject().getCode(),
                csa.getAcademicYear().getId(),
                csa.getAcademicYear().getName(),
                csa.getSubjectType(),
                csa.getStatus()
        );
    }

    private StudentSubjectEnrollmentResponse toEnrollmentResponse(StudentSubjectEnrollment e) {
        String studentName = e.getStudent().getFirstName() != null
                ? (e.getStudent().getFirstName() + " " + (e.getStudent().getLastName() != null ? e.getStudent().getLastName() : "")).trim()
                : e.getStudent().getName();
        return new StudentSubjectEnrollmentResponse(
                e.getId(),
                e.getSchool().getId(),
                e.getStudent().getId(),
                studentName,
                e.getStudent().getAdmissionNo(),
                e.getSubject().getId(),
                e.getSubject().getName(),
                e.getSubject().getCode(),
                e.getAcademicYear().getId(),
                e.getAcademicYear().getName(),
                e.getEnrollmentType(),
                e.getStatus()
        );
    }

    // =========================================================
    // --- Timetable Entries (Weekly Grid & Daily Schedule) ---
    // =========================================================

    public List<TimetableEntryResponse> getTimetableGrid(Long schoolId, Long classId, Long sectionId, String dayOfWeek, Long academicYearId, Long teacherId) {
        Long effectiveSchoolId = authContextService.resolveSchoolId(schoolId);
        entitlementService.enforceModuleAccess(effectiveSchoolId, "ACADEMICS");

        Long effectiveYearId = resolveActiveYear(effectiveSchoolId, academicYearId);
        if (effectiveYearId == null) return List.of();

        List<TimetableEntry> entries;
        if (teacherId != null) {
            if (dayOfWeek != null && !dayOfWeek.isBlank()) {
                entries = timetableEntryRepository.findBySchoolIdAndTeacherIdAndDayOfWeekAndAcademicYearId(
                        effectiveSchoolId, teacherId, dayOfWeek.toUpperCase(), effectiveYearId);
            } else {
                entries = timetableEntryRepository.findBySchoolIdAndTeacherIdAndAcademicYearId(
                        effectiveSchoolId, teacherId, effectiveYearId);
            }
        } else if (classId != null && sectionId != null && dayOfWeek != null && !dayOfWeek.isBlank()) {
            entries = timetableEntryRepository.findBySchoolIdAndSchoolClassIdAndSectionIdAndDayOfWeekAndAcademicYearId(
                    effectiveSchoolId, classId, sectionId, dayOfWeek.toUpperCase(), effectiveYearId);
        } else if (classId != null && sectionId != null) {
            entries = timetableEntryRepository.findBySchoolIdAndSchoolClassIdAndSectionIdAndAcademicYearId(
                    effectiveSchoolId, classId, sectionId, effectiveYearId);
        } else if (classId != null && dayOfWeek != null && !dayOfWeek.isBlank()) {
            entries = timetableEntryRepository.findBySchoolIdAndSchoolClassIdAndDayOfWeekAndAcademicYearId(
                    effectiveSchoolId, classId, dayOfWeek.toUpperCase(), effectiveYearId);
        } else if (classId != null) {
            entries = timetableEntryRepository.findBySchoolIdAndSchoolClassIdAndAcademicYearId(
                    effectiveSchoolId, classId, effectiveYearId);
        } else {
            entries = timetableEntryRepository.findAll().stream()
                    .filter(t -> t.getSchool().getId().equals(effectiveSchoolId) && t.getAcademicYear().getId().equals(effectiveYearId))
                    .toList();
        }
        return entries.stream().map(this::toTimetableEntryResponse).toList();
    }

    public List<TimetableEntryResponse> getTimetableGrid(Long schoolId, Long classId, Long sectionId, String dayOfWeek, Long academicYearId) {
        return getTimetableGrid(schoolId, classId, sectionId, dayOfWeek, academicYearId, null);
    }

    @Transactional
    public List<TimetableEntryResponse> saveTimetableGrid(BulkTimetableRequest request) {
        Long effectiveSchoolId = authContextService.resolveSchoolId(request.schoolId());
        entitlementService.enforceModuleAccess(effectiveSchoolId, "ACADEMICS");
        entitlementService.enforceSchoolActive(effectiveSchoolId);

        School school = getSchool(effectiveSchoolId);
        SchoolClass schoolClass = classRepository.findByIdAndSchoolId(request.classId(), effectiveSchoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Class not found"));
        Section section = request.sectionId() != null
                ? sectionRepository.findByIdAndSchoolId(request.sectionId(), effectiveSchoolId)
                        .orElseThrow(() -> new ResourceNotFoundException("Section not found"))
                : null;
        AcademicYear year = academicYearRepository.findByIdAndSchoolId(request.academicYearId(), effectiveSchoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Academic year not found"));

        List<TimetableEntry> savedList = new ArrayList<>();
        for (TimetableEntryRequest entryReq : request.entries()) {
            TimetablePeriod period = periodRepository.findById(entryReq.periodId())
                    .orElseThrow(() -> new ResourceNotFoundException("Period not found: " + entryReq.periodId()));

            Subject subject = entryReq.subjectId() != null
                    ? subjectRepository.findByIdAndSchoolId(entryReq.subjectId(), effectiveSchoolId).orElse(null)
                    : null;

            Staff teacher = entryReq.teacherId() != null
                    ? staffRepository.findByIdAndSchoolId(entryReq.teacherId(), effectiveSchoolId).orElse(null)
                    : null;

            Long sectionId = section != null ? section.getId() : null;
            TimetableEntry entry = timetableEntryRepository
                    .findBySchoolIdAndSchoolClassIdAndSectionIdAndAcademicYearIdAndDayOfWeekAndPeriodId(
                            effectiveSchoolId, schoolClass.getId(), sectionId, year.getId(), entryReq.dayOfWeek().toUpperCase(), period.getId())
                    .orElse(new TimetableEntry());

            entry.setSchool(school);
            entry.setSchoolClass(schoolClass);
            entry.setSection(section);
            entry.setAcademicYear(year);
            entry.setDayOfWeek(entryReq.dayOfWeek().toUpperCase());
            entry.setPeriod(period);
            entry.setSubject(subject);
            entry.setTeacher(teacher);
            entry.setRoomNumber(entryReq.roomNumber());
            entry.setStatus("ACTIVE");

            savedList.add(timetableEntryRepository.save(entry));
        }
        return savedList.stream().map(this::toTimetableEntryResponse).toList();
    }

    @Transactional
    public void deleteTimetableEntry(Long id, Long schoolId) {
        Long effectiveSchoolId = authContextService.resolveSchoolId(schoolId);
        entitlementService.enforceModuleAccess(effectiveSchoolId, "ACADEMICS");
        entitlementService.enforceSchoolActive(effectiveSchoolId);
        TimetableEntry entry = timetableEntryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Timetable entry not found"));
        if (!entry.getSchool().getId().equals(effectiveSchoolId)) {
            throw new ResourceNotFoundException("Timetable entry not found");
        }
        timetableEntryRepository.delete(entry);
    }

    private TimetableEntryResponse toTimetableEntryResponse(TimetableEntry e) {
        String teacherName = null;
        if (e.getTeacher() != null) {
            String fn = e.getTeacher().getFirstName() != null ? e.getTeacher().getFirstName() : "";
            String ln = e.getTeacher().getLastName() != null ? e.getTeacher().getLastName() : "";
            teacherName = (fn + " " + ln).trim();
            if (teacherName.isEmpty()) {
                teacherName = e.getTeacher().getDesignation() != null ? e.getTeacher().getDesignation() : ("Staff #" + e.getTeacher().getId());
            }
        }

        return new TimetableEntryResponse(
                e.getId(),
                e.getSchool().getId(),
                e.getSchoolClass().getId(),
                e.getSchoolClass().getName(),
                e.getSection() != null ? e.getSection().getId() : null,
                e.getSection() != null ? e.getSection().getName() : null,
                e.getAcademicYear().getId(),
                e.getAcademicYear().getName(),
                e.getDayOfWeek(),
                e.getPeriod().getId(),
                e.getPeriod().getPeriodNumber(),
                e.getPeriod().getName(),
                e.getPeriod().getStartTime(),
                e.getPeriod().getEndTime(),
                e.getPeriod().getIsBreak(),
                e.getSubject() != null ? e.getSubject().getId() : null,
                e.getSubject() != null ? e.getSubject().getName() : null,
                e.getSubject() != null ? e.getSubject().getCode() : null,
                e.getTeacher() != null ? e.getTeacher().getId() : null,
                teacherName,
                e.getRoomNumber(),
                e.getStatus()
        );
    }
}


