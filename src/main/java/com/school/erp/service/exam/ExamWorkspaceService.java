package com.school.erp.service.exam;

import com.school.erp.dto.exam.workspace.ExamWorkspaceOverviewResponse;
import com.school.erp.dto.exam.workspace.ExamSubjectConfigDto;
import com.school.erp.dto.exam.workspace.ExamApplicabilityDto;
import com.school.erp.dto.exam.workspace.ExamStudentEligibilityDto;
import com.school.erp.entity.exam.*;
import com.school.erp.exception.ResourceNotFoundException;
import com.school.erp.repository.exam.*;
import com.school.erp.security.AuthContextService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import com.school.erp.dto.exam.workspace.ExamSubjectBulkAddRequest;
import com.school.erp.dto.exam.workspace.ExamSubjectConfigUpdateRequest;
import com.school.erp.dto.exam.workspace.ExamApplicabilitySyncRequest;
import com.school.erp.entity.academic.Subject;
import com.school.erp.entity.academic.SchoolClass;
import com.school.erp.entity.academic.Section;
import com.school.erp.entity.student.Student;
import com.school.erp.repository.academic.SubjectRepository;
import com.school.erp.repository.academic.SchoolClassRepository;
import com.school.erp.repository.academic.SectionRepository;
import com.school.erp.repository.student.StudentRepository;
import java.math.BigDecimal;
import java.util.ArrayList;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExamWorkspaceService {

    private final AuthContextService authContextService;
    private final ExamSetupRepository examSetupRepository;
    private final ExamSubjectConfigRepository examSubjectConfigRepository;
    private final ExamApplicabilityRepository examApplicabilityRepository;
    private final ExamStudentEligibilityRepository examStudentEligibilityRepository;
    private final ExamScheduleRepository examScheduleRepository;
    private final SubjectRepository subjectRepository;
    private final SchoolClassRepository schoolClassRepository;
    private final SectionRepository sectionRepository;
    private final StudentRepository studentRepository;


    @Transactional(readOnly = true)
    public ExamWorkspaceOverviewResponse getOverview(Long rawSchoolId, Long examSetupId) {
        Long schoolId = authContextService.resolveSchoolId(rawSchoolId);
        ExamSetup setup = examSetupRepository.findByIdAndSchoolId(examSetupId, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Exam setup not found"));

        int subjectCount = examSubjectConfigRepository.findBySchoolIdAndExamSetupId(schoolId, examSetupId).size();
        int classCount = examApplicabilityRepository.countBySchoolIdAndExamSetupId(schoolId, examSetupId);
        int scheduleCount = examScheduleRepository.countBySchoolIdAndExamSetupId(schoolId, examSetupId);

        return ExamWorkspaceOverviewResponse.builder()
                .id(setup.getId())
                .name(setup.getName())
                .termId(setup.getTerm() != null ? setup.getTerm().getId() : null)
                .termName(setup.getTerm() != null ? setup.getTerm().getName() : null)
                .examType(setup.getGroupName())
                .status(setup.getStatus())
                .evaluationType(setup.getEvaluationType())
                .subjectCount(subjectCount)
                .classCount(classCount)
                .studentCount(0) // We can calculate this by counting unique students in student eligibility
                .scheduleCount(scheduleCount)
                .build();
    }

    @Transactional(readOnly = true)
    public List<ExamSubjectConfigDto> getSubjects(Long rawSchoolId, Long examSetupId) {
        Long schoolId = authContextService.resolveSchoolId(rawSchoolId);
        return examSubjectConfigRepository.findBySchoolIdAndExamSetupId(schoolId, examSetupId).stream()
                .map(c -> ExamSubjectConfigDto.builder()
                        .id(c.getId())
                        .subjectId(c.getSubject().getId())
                        .subjectName(c.getSubject().getName())
                        .subjectCode(c.getSubject().getCode())
                        .maxMarks(c.getMaxMarks())
                        .passingMarks(c.getPassingMarks())
                        .theoryMarks(c.getTheoryMarks())
                        .practicalMarks(c.getPracticalMarks())
                        .internalMarks(c.getInternalMarks())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ExamApplicabilityDto> getApplicabilities(Long rawSchoolId, Long examSetupId) {
        Long schoolId = authContextService.resolveSchoolId(rawSchoolId);
        return examApplicabilityRepository.findBySchoolIdAndExamSetupId(schoolId, examSetupId).stream()
                .map(a -> ExamApplicabilityDto.builder()
                        .id(a.getId())
                        .classId(a.getSchoolClass().getId())
                        .className(a.getSchoolClass().getName())
                        .sectionId(a.getSection() != null ? a.getSection().getId() : null)
                        .sectionName(a.getSection() != null ? a.getSection().getName() : "All")
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ExamStudentEligibilityDto> getEligibleStudents(Long rawSchoolId, Long examSetupId) {
        Long schoolId = authContextService.resolveSchoolId(rawSchoolId);
        List<ExamStudentEligibility> allEligibilities = examStudentEligibilityRepository.findBySchoolIdAndExamSetupId(schoolId, examSetupId);
        
        java.util.Map<Long, List<ExamStudentEligibility>> groupedByStudent = allEligibilities.stream()
                .collect(Collectors.groupingBy(e -> e.getStudent().getId()));
                
        return groupedByStudent.entrySet().stream().map(entry -> {
            Student student = entry.getValue().get(0).getStudent();
            List<com.school.erp.dto.exam.workspace.ExamStudentSubjectEligibilityDto> subjects = entry.getValue().stream()
                .map(e -> com.school.erp.dto.exam.workspace.ExamStudentSubjectEligibilityDto.builder()
                        .eligibilityId(e.getId())
                        .subjectId(e.getExamSubjectConfig().getSubject().getId())
                        .subjectName(e.getExamSubjectConfig().getSubject().getName())
                        .subjectCode(e.getExamSubjectConfig().getSubject().getCode())
                        .status(e.getStatus())
                        .build())
                .collect(Collectors.toList());
                
            boolean allExempted = subjects.stream().allMatch(s -> "EXEMPTED".equals(s.getStatus()));
            String overallStatus = allExempted ? "EXEMPTED" : "ELIGIBLE";
                
            return ExamStudentEligibilityDto.builder()
                    .id(student.getId()) // Using studentId as the ID for the grouped record
                    .studentId(student.getId())
                    .studentName(student.getFirstName() + " " + (student.getLastName() != null ? student.getLastName() : ""))
                    .admissionNo(student.getAdmissionNo())
                    .classId(student.getSchoolClass() != null ? student.getSchoolClass().getId() : null)
                    .className(student.getSchoolClass() != null ? student.getSchoolClass().getName() : "")
                    .sectionId(student.getSectionId())
                    .sectionName(student.getSectionId() != null ? "Section " + student.getSectionId() : "")
                    .status(overallStatus)
                    .remarks(null)
                    .subjects(subjects)
                    .build();
        }).collect(Collectors.toList());
    }

    @Transactional
    public void updateStudentEligibility(Long rawSchoolId, Long examSetupId, Long studentId, com.school.erp.dto.exam.workspace.ExamStudentEligibilityStatusUpdateRequest request) {
        Long schoolId = authContextService.resolveSchoolId(rawSchoolId);
        List<ExamStudentEligibility> eligibilities = examStudentEligibilityRepository.findBySchoolIdAndExamSetupId(schoolId, examSetupId).stream()
                .filter(e -> e.getStudent().getId().equals(studentId))
                .collect(Collectors.toList());
                
        if (eligibilities.isEmpty()) {
            throw new ResourceNotFoundException("No eligibility records found for student");
        }
        
        for (ExamStudentEligibility e : eligibilities) {
            e.setStatus(request.getStatus());
            examStudentEligibilityRepository.save(e);
        }
    }
    
    @Transactional
    public void updateStudentSubjectEligibility(Long rawSchoolId, Long examSetupId, Long eligibilityId, com.school.erp.dto.exam.workspace.ExamStudentEligibilityStatusUpdateRequest request) {
        Long schoolId = authContextService.resolveSchoolId(rawSchoolId);
        ExamStudentEligibility eligibility = examStudentEligibilityRepository.findById(eligibilityId)
                .orElseThrow(() -> new ResourceNotFoundException("Eligibility record not found"));
        
        if (!eligibility.getSchool().getId().equals(schoolId)) {
            throw new com.school.erp.exception.UnauthorizedException("Unauthorized access to eligibility record");
        }
        
        eligibility.setStatus(request.getStatus());
        examStudentEligibilityRepository.save(eligibility);
    }

    @Transactional
    public void addSubjects(Long rawSchoolId, Long examSetupId, ExamSubjectBulkAddRequest request) {
        Long schoolId = authContextService.resolveSchoolId(rawSchoolId);
        ExamSetup setup = examSetupRepository.findByIdAndSchoolId(examSetupId, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Exam setup not found"));

        for (Long subjectId : request.getSubjectIds()) {
            boolean exists = examSubjectConfigRepository.findBySchoolIdAndExamSetupId(schoolId, examSetupId).stream()
                    .anyMatch(c -> c.getSubject().getId().equals(subjectId));
            if (!exists) {
                Subject subject = subjectRepository.findById(subjectId)
                        .orElseThrow(() -> new ResourceNotFoundException("Subject not found: " + subjectId));
                ExamSubjectConfig config = new ExamSubjectConfig();
                config.setSchool(setup.getSchool());
                config.setExamSetup(setup);
                config.setSubject(subject);
                config.setMaxMarks(new BigDecimal("100.00"));
                config.setPassingMarks(new BigDecimal("35.00"));
                examSubjectConfigRepository.save(config);
            }
        }
    }

    @Transactional
    public void removeSubject(Long rawSchoolId, Long examSetupId, Long configId) {
        Long schoolId = authContextService.resolveSchoolId(rawSchoolId);
        ExamSubjectConfig config = examSubjectConfigRepository.findById(configId)
                .orElseThrow(() -> new ResourceNotFoundException("Subject config not found"));
        if (!config.getSchool().getId().equals(schoolId) || !config.getExamSetup().getId().equals(examSetupId)) {
            throw new com.school.erp.exception.UnauthorizedException("Unauthorized access to subject config");
        }
        examSubjectConfigRepository.delete(config);
    }

    @Transactional
    public ExamSubjectConfigDto updateSubjectConfig(Long rawSchoolId, Long examSetupId, Long configId, ExamSubjectConfigUpdateRequest request) {
        Long schoolId = authContextService.resolveSchoolId(rawSchoolId);
        ExamSubjectConfig config = examSubjectConfigRepository.findById(configId)
                .orElseThrow(() -> new ResourceNotFoundException("Subject config not found"));
        if (!config.getSchool().getId().equals(schoolId) || !config.getExamSetup().getId().equals(examSetupId)) {
            throw new com.school.erp.exception.UnauthorizedException("Unauthorized access to subject config");
        }
        
        config.setMaxMarks(request.getMaxMarks());
        config.setPassingMarks(request.getPassingMarks());
        config.setTheoryMarks(request.getTheoryMarks());
        config.setPracticalMarks(request.getPracticalMarks());
        config.setInternalMarks(request.getInternalMarks());
        examSubjectConfigRepository.save(config);
        
        return ExamSubjectConfigDto.builder()
                .id(config.getId())
                .subjectId(config.getSubject().getId())
                .subjectName(config.getSubject().getName())
                .subjectCode(config.getSubject().getCode())
                .maxMarks(config.getMaxMarks())
                .passingMarks(config.getPassingMarks())
                .theoryMarks(config.getTheoryMarks())
                .practicalMarks(config.getPracticalMarks())
                .internalMarks(config.getInternalMarks())
                .build();
    }

    @Transactional
    public void syncApplicabilities(Long rawSchoolId, Long examSetupId, ExamApplicabilitySyncRequest request) {
        Long schoolId = authContextService.resolveSchoolId(rawSchoolId);
        ExamSetup setup = examSetupRepository.findByIdAndSchoolId(examSetupId, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Exam setup not found"));

        // For simplicity, we remove existing and re-add to sync. 
        // Note: CASCADE constraints on exam_student_eligibility might drop eligibility if we delete applicabilities.
        // A better approach is to compute diff.
        
        List<ExamApplicability> existing = examApplicabilityRepository.findBySchoolIdAndExamSetupId(schoolId, examSetupId);
        
        // Find what to add
        for (ExamApplicabilitySyncRequest.ClassSectionPair pair : request.getApplicabilities()) {
            boolean found = existing.stream().anyMatch(e -> 
                e.getSchoolClass().getId().equals(pair.getClassId()) && 
                ((e.getSection() == null && pair.getSectionId() == null) || (e.getSection() != null && e.getSection().getId().equals(pair.getSectionId())))
            );
            
            if (!found) {
                // Add new applicability
                SchoolClass schoolClass = schoolClassRepository.findById(pair.getClassId()).orElse(null);
                Section section = pair.getSectionId() != null ? sectionRepository.findById(pair.getSectionId()).orElse(null) : null;
                
                if (schoolClass != null) {
                    ExamApplicability newApp = new ExamApplicability();
                    newApp.setSchool(setup.getSchool());
                    newApp.setExamSetup(setup);
                    newApp.setSchoolClass(schoolClass);
                    newApp.setSection(section);
                    examApplicabilityRepository.save(newApp);
                    
                    // Automatically enroll active students from this class/section
                    List<Student> students;
                    if (section != null) {
                        students = studentRepository.findBySchoolIdAndSchoolClassIdAndSectionId(schoolId, schoolClass.getId(), section.getId());
                    } else {
                        students = studentRepository.findBySchoolIdAndSchoolClassId(schoolId, schoolClass.getId());
                    }
                    
                    List<ExamStudentEligibility> existingEligibilities = examStudentEligibilityRepository.findBySchoolIdAndExamSetupId(schoolId, examSetupId);
                    List<ExamSubjectConfig> configs = examSubjectConfigRepository.findBySchoolIdAndExamSetupId(schoolId, examSetupId);
                    
                    for (Student s : students) {
                        for (ExamSubjectConfig config : configs) {
                            boolean alreadyEnrolled = existingEligibilities.stream()
                                .anyMatch(ee -> ee.getStudent().getId().equals(s.getId()) && ee.getExamSubjectConfig().getId().equals(config.getId()));
                            if (!alreadyEnrolled) {
                                ExamStudentEligibility eligibility = new ExamStudentEligibility();
                                eligibility.setSchool(setup.getSchool());
                                eligibility.setExamSubjectConfig(config);
                                eligibility.setStudent(s);
                                eligibility.setStatus("ELIGIBLE");
                                examStudentEligibilityRepository.save(eligibility);
                            }
                        }
                    }
                }
            }
        }
        
        // Find what to remove
        for (ExamApplicability ext : existing) {
            boolean keep = request.getApplicabilities().stream().anyMatch(pair -> 
                ext.getSchoolClass().getId().equals(pair.getClassId()) && 
                ((ext.getSection() == null && pair.getSectionId() == null) || (ext.getSection() != null && ext.getSection().getId().equals(pair.getSectionId())))
            );
            if (!keep) {
                examApplicabilityRepository.delete(ext);
                
                List<ExamStudentEligibility> existingEligibilities = examStudentEligibilityRepository.findBySchoolIdAndExamSetupId(schoolId, examSetupId);
                
                for (ExamStudentEligibility ee : existingEligibilities) {
                    Student s = ee.getStudent();
                    if (s.getSchoolClass() != null && s.getSchoolClass().getId().equals(ext.getSchoolClass().getId())) {
                        if (ext.getSection() == null || (s.getSectionId() != null && s.getSectionId().equals(ext.getSection().getId()))) {
                            examStudentEligibilityRepository.delete(ee);
                        }
                    }
                }
            }
        }
    }

    @Transactional(readOnly = true)
    public List<com.school.erp.dto.exam.ExamScheduleResponse> getExamSchedules(Long rawSchoolId, Long examSetupId) {
        Long schoolId = authContextService.resolveSchoolId(rawSchoolId);
        List<ExamSchedule> schedules = examScheduleRepository.findBySchoolIdAndExamSetupId(schoolId, examSetupId);
        
        return schedules.stream().map(s -> com.school.erp.dto.exam.ExamScheduleResponse.builder()
            .id(s.getId())
            .schoolId(s.getSchool().getId())
            .examSetupId(s.getExamSetup().getId())
            .classId(s.getSchoolClass().getId())
            .sectionId(s.getSection() != null ? s.getSection().getId() : null)
            .subjectId(s.getSubject().getId())
            .subjectCode(s.getSubject().getCode())
            .subjectName(s.getSubject().getName())
            .examDate(s.getExamDate())
            .startTime(s.getStartTime())
            .endTime(s.getEndTime())
            .roomNumber(s.getRoomNumber())
            .instructions(s.getInstructions())
            .status(s.getStatus())
            .build()
        ).collect(Collectors.toList());
    }

    @Transactional
    public void saveExamSchedules(Long rawSchoolId, Long examSetupId, com.school.erp.dto.exam.BulkExamScheduleRequest request) {
        Long schoolId = authContextService.resolveSchoolId(rawSchoolId);
        ExamSetup setup = examSetupRepository.findByIdAndSchoolId(examSetupId, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Exam setup not found"));

        SchoolClass schoolClass = schoolClassRepository.findById(request.getClassId())
                .orElseThrow(() -> new ResourceNotFoundException("Class not found"));
                
        Section section = request.getSectionId() != null ? sectionRepository.findById(request.getSectionId()).orElse(null) : null;

        // Fetch existing schedules for this class/section and exam setup
        List<ExamSchedule> existingSchedules = examScheduleRepository.findBySchoolIdAndExamSetupId(schoolId, examSetupId)
            .stream()
            .filter(s -> s.getSchoolClass().getId().equals(schoolClass.getId()) &&
                         (section == null ? s.getSection() == null : (s.getSection() != null && s.getSection().getId().equals(section.getId()))))
            .collect(Collectors.toList());

        for (com.school.erp.dto.exam.ExamScheduleRequest sr : request.getSchedules()) {
            ExamSchedule schedule = existingSchedules.stream()
                .filter(s -> s.getSubject().getId().equals(sr.getSubjectId()))
                .findFirst()
                .orElse(new ExamSchedule());
                
            if (schedule.getId() == null) {
                schedule.setSchool(setup.getSchool());
                schedule.setExamSetup(setup);
                schedule.setSchoolClass(schoolClass);
                schedule.setSection(section);
                
                Subject subject = subjectRepository.findById(sr.getSubjectId())
                    .orElseThrow(() -> new ResourceNotFoundException("Subject not found"));
                schedule.setSubject(subject);
                
                ExamSubjectConfig config = examSubjectConfigRepository.findBySchoolIdAndExamSetupId(schoolId, examSetupId).stream()
                    .filter(c -> c.getSubject().getId().equals(subject.getId()))
                    .findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("Exam Subject Config not found for subject: " + subject.getName()));
                schedule.setExamSubjectConfig(config);
            }
            
            schedule.setExamDate(sr.getExamDate());
            schedule.setStartTime(sr.getStartTime());
            schedule.setEndTime(sr.getEndTime());
            schedule.setRoomNumber(sr.getRoomNumber());
            schedule.setInstructions(sr.getInstructions());
            schedule.setStatus(sr.getStatus() != null ? sr.getStatus() : "SCHEDULED");
            
            examScheduleRepository.save(schedule);
        }
    }
}
