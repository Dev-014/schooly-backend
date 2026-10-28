package com.school.erp.service.hr;

import com.school.erp.entity.academic.AcademicYear;
import com.school.erp.entity.academic.SchoolClass;
import com.school.erp.entity.academic.Section;
import com.school.erp.entity.academic.Subject;
import com.school.erp.entity.auth.User;
import com.school.erp.entity.auth.UserAssignment;
import com.school.erp.entity.hr.SchoolDepartment;
import com.school.erp.entity.hr.Staff;
import com.school.erp.entity.superadmin.School;
import com.school.erp.repository.academic.AcademicYearRepository;
import com.school.erp.repository.academic.SchoolClassRepository;
import com.school.erp.repository.academic.SectionRepository;
import com.school.erp.repository.academic.SubjectRepository;
import com.school.erp.repository.auth.UserAssignmentRepository;
import com.school.erp.repository.auth.UserRepository;
import com.school.erp.repository.hr.SchoolDepartmentRepository;
import com.school.erp.repository.hr.StaffRepository;
import com.school.erp.repository.academic.ClassTeacherAssignmentRepository;
import com.school.erp.repository.academic.TimetableEntryRepository;
import com.school.erp.entity.academic.ClassTeacherAssignment;
import com.school.erp.entity.academic.TimetableEntry;
import com.school.erp.repository.superadmin.SchoolRepository;
import com.school.erp.dto.auth.UserAssignmentRequest;
import com.school.erp.dto.auth.UserAssignmentResponse;
import com.school.erp.dto.staff.StaffRequest;
import com.school.erp.entity.hr.StaffBankAccount;
import com.school.erp.entity.hr.StaffDocument;
import com.school.erp.repository.hr.StaffBankAccountRepository;
import com.school.erp.repository.hr.StaffDocumentRepository;
import com.school.erp.dto.hr.StaffBankAccountDTO;
import com.school.erp.dto.hr.StaffBankAccountRequest;
import com.school.erp.dto.hr.StaffDocumentDTO;
import com.school.erp.dto.hr.StaffDocumentRequest;
import lombok.RequiredArgsConstructor;
import java.util.stream.Collectors;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HrStaffService {

    private final StaffRepository staffRepository;
    private final SchoolRepository schoolRepository;
    private final UserRepository userRepository;
    private final UserAssignmentRepository userAssignmentRepository;
    private final SchoolClassRepository classRepository;
    private final SectionRepository sectionRepository;
    private final SubjectRepository subjectRepository;
    private final AcademicYearRepository academicYearRepository;
    private final SchoolDepartmentRepository schoolDepartmentRepository;
    private final StaffDocumentRepository staffDocumentRepository;
    private final StaffBankAccountRepository staffBankAccountRepository;
    private final ClassTeacherAssignmentRepository classTeacherAssignmentRepository;
    private final TimetableEntryRepository timetableEntryRepository;

    public List<Staff> getStaffBySchool(Long schoolId) {
        return staffRepository.findBySchoolId(schoolId);
    }

    public Staff getStaffById(Long schoolId, Long staffId) {
        return staffRepository.findById(staffId)
                .filter(staff -> staff.getSchool().getId().equals(schoolId))
                .orElseThrow(() -> new RuntimeException("Staff not found"));
    }

    @Transactional
    public Staff createStaff(Long schoolId, StaffRequest request) {
        School school = schoolRepository.findById(schoolId)
                .orElseThrow(() -> new RuntimeException("School not found"));

        Staff staff = new Staff();
        staff.setSchool(school);
        updateStaffFromRequest(staff, request);
        
        return staffRepository.save(staff);
    }

    @Transactional
    public Staff updateStaff(Long schoolId, Long staffId, StaffRequest request) {
        Staff staff = getStaffById(schoolId, staffId);
        updateStaffFromRequest(staff, request);
        return staffRepository.save(staff);
    }

    private void updateStaffFromRequest(Staff staff, StaffRequest request) {
        staff.setDepartmentId(request.departmentId());
        staff.setDesignationId(request.designationId());
        staff.setJoiningDate(request.joiningDate());
        staff.setSalary(request.salary());
        staff.setStatus(request.status());
        staff.setFirstName(request.firstName());
        staff.setLastName(request.lastName());
        staff.setDepartment(request.department());
        staff.setDesignation(request.designation());
        staff.setPhotoUrl(request.photoUrl());
        staff.setPhone(request.phone());
        staff.setEmail(request.email());
        staff.setBiometricId(request.biometricId());
        staff.setDateOfBirth(request.dateOfBirth());
        staff.setGender(request.gender());
        staff.setMaritalStatus(request.maritalStatus());
        staff.setFatherName(request.fatherName());
        staff.setMotherName(request.motherName());
        staff.setEmergencyContact(request.emergencyContact());
        staff.setCurrentAddress(request.currentAddress());
        staff.setPermanentAddress(request.permanentAddress());
        staff.setQualification(request.qualification());
        staff.setWorkExperience(request.workExperience());
        staff.setContractType(request.contractType());
        staff.setWorkShift(request.workShift());
        staff.setLocation(request.location());
        staff.setNotes(request.notes());
    }

    public List<UserAssignmentResponse> getStaffAssignments(Long schoolId, Long staffId) {
        Staff staff = getStaffById(schoolId, staffId);
        List<UserAssignmentResponse> result = new java.util.ArrayList<>();
        java.util.Set<String> seenKeys = new java.util.HashSet<>();

        // 1. From Academic SOT: Class Teacher Assignments
        List<ClassTeacherAssignment> classTeacherAssignments =
                classTeacherAssignmentRepository.findBySchoolIdAndStaffIdAndStatus(schoolId, staff.getId(), "ACTIVE");
        for (ClassTeacherAssignment cta : classTeacherAssignments) {
            String key = "class_teacher_" + cta.getSchoolClass().getId() + "_" + (cta.getSection() != null ? cta.getSection().getId() : "all");
            if (seenKeys.add(key)) {
                result.add(UserAssignmentResponse.builder()
                        .id(cta.getId())
                        .schoolId(cta.getSchool().getId())
                        .userId(staff.getUserId())
                        .academicSessionId(cta.getAcademicYear() != null ? cta.getAcademicYear().getId() : null)
                        .sessionName(cta.getAcademicYear() != null ? cta.getAcademicYear().getName() : null)
                        .assignmentType("class_teacher")
                        .classId(cta.getSchoolClass().getId())
                        .className(cta.getSchoolClass().getName())
                        .sectionId(cta.getSection() != null ? cta.getSection().getId() : null)
                        .sectionName(cta.getSection() != null ? cta.getSection().getName() : null)
                        .effectiveFrom(cta.getCreatedAt())
                        .isActive(true)
                        .source("ACADEMIC_CLASS_TEACHER")
                        .build());
            }
        }

        // 2. From Academic SOT: Timetable Entries (Subject Teacher Assignments)
        List<TimetableEntry> timetableEntries =
                timetableEntryRepository.findBySchoolIdAndTeacherId(schoolId, staff.getId());
        for (TimetableEntry te : timetableEntries) {
            if ("ACTIVE".equalsIgnoreCase(te.getStatus()) && te.getSubject() != null) {
                String key = "subject_teacher_" + te.getSchoolClass().getId() + "_" +
                        (te.getSection() != null ? te.getSection().getId() : "all") + "_" +
                        te.getSubject().getId();
                if (seenKeys.add(key)) {
                    result.add(UserAssignmentResponse.builder()
                            .id(te.getId())
                            .schoolId(te.getSchool().getId())
                            .userId(staff.getUserId())
                            .academicSessionId(te.getAcademicYear() != null ? te.getAcademicYear().getId() : null)
                            .sessionName(te.getAcademicYear() != null ? te.getAcademicYear().getName() : null)
                            .assignmentType("subject_teacher")
                            .classId(te.getSchoolClass().getId())
                            .className(te.getSchoolClass().getName())
                            .sectionId(te.getSection() != null ? te.getSection().getId() : null)
                            .sectionName(te.getSection() != null ? te.getSection().getName() : null)
                            .subjectId(te.getSubject().getId())
                            .subjectName(te.getSubject().getName())
                            .subjectCode(te.getSubject().getCode())
                            .isActive(true)
                            .source("ACADEMIC_TIMETABLE")
                            .build());
                }
            }
        }

        // 3. From user_assignments table (HR specific / Department responsibilities)
        if (staff.getUserId() != null) {
            List<UserAssignment> userAssignments =
                    userAssignmentRepository.findBySchoolIdAndUserIdAndIsActiveTrue(schoolId, staff.getUserId());
            for (UserAssignment ua : userAssignments) {
                String key = ua.getAssignmentType() + "_" + ua.getClassId() + "_" +
                        (ua.getSectionId() != null ? ua.getSectionId() : "all") + "_" +
                        ua.getSubjectId() + "_" + ua.getDepartmentId();
                if (seenKeys.add(key)) {
                    result.add(toAssignmentResponse(ua));
                }
            }
        }

        return result;
    }

    @Transactional
    public UserAssignmentResponse assignRole(Long schoolId, Long staffId, UserAssignmentRequest request) {
        Staff staff = getStaffById(schoolId, staffId);

        // 1. Direct Academic SOT: Class Teacher Assignments go directly to class_teacher_assignments
        if ("class_teacher".equalsIgnoreCase(request.assignmentType())) {
            School school = schoolRepository.findById(schoolId)
                    .orElseThrow(() -> new RuntimeException("School not found"));
            SchoolClass schoolClass = classRepository.findByIdAndSchoolId(request.classId(), schoolId)
                    .orElseThrow(() -> new RuntimeException("Class not found for ID: " + request.classId()));
            Section section = request.sectionId() != null
                    ? sectionRepository.findByIdAndSchoolId(request.sectionId(), schoolId).orElse(null)
                    : null;

            Long yearId = request.academicSessionId();
            AcademicYear year = yearId != null
                    ? academicYearRepository.findByIdAndSchoolId(yearId, schoolId).orElse(null)
                    : academicYearRepository.findBySchoolId(schoolId).stream()
                            .filter(y -> "ACTIVE".equalsIgnoreCase(y.getStatus()))
                            .findFirst()
                            .orElse(null);

            if (year == null) {
                throw new RuntimeException("Active Academic Year not found for school " + schoolId);
            }

            // Deactivate previous active class teacher assignment for this section and year
            if (section != null) {
                classTeacherAssignmentRepository.findBySchoolIdAndSectionIdAndAcademicYearIdAndStatus(schoolId, section.getId(), year.getId(), "ACTIVE")
                        .ifPresent(existing -> {
                            existing.setStatus("INACTIVE");
                            classTeacherAssignmentRepository.saveAndFlush(existing);
                        });
            }

            ClassTeacherAssignment assignment = new ClassTeacherAssignment();
            assignment.setSchool(school);
            assignment.setStaff(staff);
            assignment.setSchoolClass(schoolClass);
            assignment.setSection(section);
            assignment.setAcademicYear(year);
            assignment.setStatus("ACTIVE");

            ClassTeacherAssignment saved = classTeacherAssignmentRepository.save(assignment);

            return UserAssignmentResponse.builder()
                    .id(saved.getId())
                    .schoolId(schoolId)
                    .userId(staff.getUserId())
                    .academicSessionId(year.getId())
                    .sessionName(year.getName())
                    .assignmentType("class_teacher")
                    .classId(schoolClass.getId())
                    .className(schoolClass.getName())
                    .sectionId(section != null ? section.getId() : null)
                    .sectionName(section != null ? section.getName() : null)
                    .effectiveFrom(saved.getCreatedAt() != null ? saved.getCreatedAt() : LocalDateTime.now())
                    .isActive(true)
                    .source("ACADEMIC_CLASS_TEACHER")
                    .build();
        }

        // 2. Non-academic / Department scopes are stored in user_assignments
        if (staff.getUserId() == null) {
            throw new RuntimeException("Staff does not have an associated user account");
        }
        User user = userRepository.findById(staff.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserAssignment assignment = new UserAssignment();
        assignment.setSchoolId(schoolId);
        assignment.setUser(user);
        assignment.setAcademicSessionId(request.academicSessionId());
        assignment.setAssignmentType(request.assignmentType());
        assignment.setClassId(request.classId());
        assignment.setSectionId(request.sectionId());
        assignment.setSubjectId(request.subjectId());
        assignment.setDepartmentId(request.departmentId());
        assignment.setEffectiveFrom(LocalDateTime.now());
        assignment.setActive(true);

        return toAssignmentResponse(userAssignmentRepository.save(assignment));
    }

    @Transactional
    public void revokeAssignment(Long schoolId, Long staffId, Long assignmentId) {
        Staff staff = getStaffById(schoolId, staffId);

        // 1. Try user_assignments table
        java.util.Optional<UserAssignment> uaOpt = userAssignmentRepository.findById(assignmentId);
        if (uaOpt.isPresent()) {
            UserAssignment assignment = uaOpt.get();
            if (assignment.getSchoolId().equals(schoolId) && staff.getUserId() != null && assignment.getUser().getId().equals(staff.getUserId())) {
                assignment.setActive(false);
                assignment.setEffectiveTo(LocalDateTime.now());
                userAssignmentRepository.save(assignment);
                return;
            }
        }

        // 2. Try class_teacher_assignments table
        java.util.Optional<ClassTeacherAssignment> ctaOpt = classTeacherAssignmentRepository.findById(assignmentId);
        if (ctaOpt.isPresent()) {
            ClassTeacherAssignment cta = ctaOpt.get();
            if (cta.getSchool().getId().equals(schoolId) && cta.getStaff().getId().equals(staff.getId())) {
                cta.setStatus("INACTIVE");
                classTeacherAssignmentRepository.save(cta);
                return;
            }
        }

        // 3. Try timetable_entries table
        java.util.Optional<TimetableEntry> teOpt = timetableEntryRepository.findById(assignmentId);
        if (teOpt.isPresent()) {
            TimetableEntry te = teOpt.get();
            if (te.getSchool().getId().equals(schoolId) && te.getTeacher() != null && te.getTeacher().getId().equals(staff.getId())) {
                te.setStatus("INACTIVE");
                timetableEntryRepository.save(te);
                return;
            }
        }

        throw new RuntimeException("Assignment not found or does not belong to this staff");
    }

    private UserAssignmentResponse toAssignmentResponse(UserAssignment ua) {
        String className = null;
        if (ua.getClassId() != null) {
            className = classRepository.findById(ua.getClassId()).map(SchoolClass::getName).orElse(null);
        }

        String sectionName = null;
        if (ua.getSectionId() != null) {
            sectionName = sectionRepository.findById(ua.getSectionId()).map(Section::getName).orElse(null);
        }

        String subjectName = null;
        String subjectCode = null;
        if (ua.getSubjectId() != null) {
            Subject sub = subjectRepository.findById(ua.getSubjectId()).orElse(null);
            if (sub != null) {
                subjectName = sub.getName();
                subjectCode = sub.getCode();
            }
        }

        String sessionName = null;
        if (ua.getAcademicSessionId() != null) {
            sessionName = academicYearRepository.findById(ua.getAcademicSessionId()).map(AcademicYear::getName).orElse(null);
        }

        String departmentName = null;
        if (ua.getDepartmentId() != null) {
            departmentName = schoolDepartmentRepository.findById(ua.getDepartmentId()).map(SchoolDepartment::getName).orElse(null);
        }

        return UserAssignmentResponse.builder()
                .id(ua.getId())
                .schoolId(ua.getSchoolId())
                .userId(ua.getUser().getId())
                .academicSessionId(ua.getAcademicSessionId())
                .sessionName(sessionName)
                .assignmentType(ua.getAssignmentType())
                .classId(ua.getClassId())
                .className(className)
                .sectionId(ua.getSectionId())
                .sectionName(sectionName)
                .subjectId(ua.getSubjectId())
                .subjectName(subjectName)
                .subjectCode(subjectCode)
                .departmentId(ua.getDepartmentId())
                .departmentName(departmentName)
                .effectiveFrom(ua.getEffectiveFrom())
                .effectiveTo(ua.getEffectiveTo())
                .isActive(ua.isActive())
                .source("HR_MANUAL")
                .build();
    }

    @Transactional(readOnly = true)
    public List<StaffDocumentDTO> getStaffDocuments(Long schoolId, Long staffId) {
        Staff staff = getStaffById(schoolId, staffId);
        return staffDocumentRepository.findByStaffId(staff.getId()).stream()
                .map(doc -> StaffDocumentDTO.builder()
                        .id(doc.getId())
                        .staffId(staff.getId())
                        .documentType(doc.getDocumentType())
                        .fileName(doc.getFileName())
                        .fileUrl(doc.getFileUrl())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public StaffDocumentDTO addStaffDocument(Long schoolId, Long staffId, StaffDocumentRequest request) {
        Staff staff = getStaffById(schoolId, staffId);
        StaffDocument doc = new StaffDocument();
        doc.setStaff(staff);
        doc.setDocumentType(request.getDocumentType());
        doc.setFileName(request.getFileName());
        doc.setFileUrl(request.getFileUrl());
        StaffDocument saved = staffDocumentRepository.save(doc);

        return StaffDocumentDTO.builder()
                .id(saved.getId())
                .staffId(staff.getId())
                .documentType(saved.getDocumentType())
                .fileName(saved.getFileName())
                .fileUrl(saved.getFileUrl())
                .build();
    }

    @Transactional
    public void deleteStaffDocument(Long schoolId, Long staffId, Long docId) {
        Staff staff = getStaffById(schoolId, staffId);
        StaffDocument doc = staffDocumentRepository.findByIdAndStaffId(docId, staff.getId())
                .orElseThrow(() -> new RuntimeException("Document not found for staff"));
        staffDocumentRepository.delete(doc);
    }

    @Transactional(readOnly = true)
    public StaffBankAccountDTO getStaffBankAccount(Long schoolId, Long staffId) {
        Staff staff = getStaffById(schoolId, staffId);
        return staffBankAccountRepository.findByStaffId(staff.getId())
                .map(acc -> StaffBankAccountDTO.builder()
                        .id(acc.getId())
                        .staffId(staff.getId())
                        .accountHolderName(acc.getAccountHolderName())
                        .accountNumber(acc.getAccountNumber())
                        .bankName(acc.getBankName())
                        .ifscCode(acc.getIfscCode())
                        .branchName(acc.getBranchName())
                        .build())
                .orElse(null);
    }

    @Transactional
    public StaffBankAccountDTO updateStaffBankAccount(Long schoolId, Long staffId, StaffBankAccountRequest request) {
        Staff staff = getStaffById(schoolId, staffId);
        StaffBankAccount acc = staffBankAccountRepository.findByStaffId(staff.getId())
                .orElseGet(() -> {
                    StaffBankAccount newAcc = new StaffBankAccount();
                    newAcc.setStaff(staff);
                    return newAcc;
                });

        acc.setAccountHolderName(request.getAccountHolderName());
        acc.setAccountNumber(request.getAccountNumber());
        acc.setBankName(request.getBankName());
        acc.setIfscCode(request.getIfscCode());
        acc.setBranchName(request.getBranchName());

        StaffBankAccount saved = staffBankAccountRepository.save(acc);
        return StaffBankAccountDTO.builder()
                .id(saved.getId())
                .staffId(staff.getId())
                .accountHolderName(saved.getAccountHolderName())
                .accountNumber(saved.getAccountNumber())
                .bankName(saved.getBankName())
                .ifscCode(saved.getIfscCode())
                .branchName(saved.getBranchName())
                .build();
    }
}
