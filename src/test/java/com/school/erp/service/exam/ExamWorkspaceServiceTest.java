package com.school.erp.service.exam;

import com.school.erp.dto.exam.workspace.ExamApplicabilitySyncRequest;
import com.school.erp.dto.exam.workspace.ExamSubjectConfigDto;
import com.school.erp.dto.exam.workspace.ExamWorkspaceOverviewResponse;
import com.school.erp.entity.academic.ClassSubjectAssignment;
import com.school.erp.entity.academic.SchoolClass;
import com.school.erp.entity.academic.Section;
import com.school.erp.entity.academic.Subject;
import com.school.erp.entity.exam.ExamApplicability;
import com.school.erp.entity.exam.ExamSetup;
import com.school.erp.entity.exam.ExamStudentEligibility;
import com.school.erp.entity.exam.ExamSubjectConfig;
import com.school.erp.entity.exam.ExamTerm;
import com.school.erp.entity.student.Student;
import com.school.erp.entity.student.StudentSubjectEnrollment;
import com.school.erp.entity.superadmin.School;
import com.school.erp.repository.academic.ClassSubjectAssignmentRepository;
import com.school.erp.repository.academic.SchoolClassRepository;
import com.school.erp.repository.academic.SectionRepository;
import com.school.erp.repository.academic.SubjectRepository;
import com.school.erp.repository.exam.ExamApplicabilityRepository;
import com.school.erp.repository.exam.ExamScheduleRepository;
import com.school.erp.repository.exam.ExamSetupRepository;
import com.school.erp.repository.exam.ExamStudentEligibilityRepository;
import com.school.erp.repository.exam.ExamSubjectConfigRepository;
import com.school.erp.repository.student.StudentRepository;
import com.school.erp.repository.student.StudentSubjectEnrollmentRepository;
import com.school.erp.security.AuthContextService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExamWorkspaceServiceTest {

    @Mock
    private AuthContextService authContextService;
    @Mock
    private ExamSetupRepository examSetupRepository;
    @Mock
    private ExamSubjectConfigRepository examSubjectConfigRepository;
    @Mock
    private ExamApplicabilityRepository examApplicabilityRepository;
    @Mock
    private ExamStudentEligibilityRepository examStudentEligibilityRepository;
    @Mock
    private ExamScheduleRepository examScheduleRepository;
    @Mock
    private SubjectRepository subjectRepository;
    @Mock
    private SchoolClassRepository schoolClassRepository;
    @Mock
    private SectionRepository sectionRepository;
    @Mock
    private StudentRepository studentRepository;
    @Mock
    private ClassSubjectAssignmentRepository classSubjectAssignmentRepository;
    @Mock
    private StudentSubjectEnrollmentRepository studentSubjectEnrollmentRepository;

    @InjectMocks
    private ExamWorkspaceService examWorkspaceService;

    private School school;
    private ExamSetup examSetup;
    private SchoolClass grade10;
    private Section sectionA;
    private Subject mathSubject;
    private Subject csElectiveSubject;
    private Student student1;
    private Student student2;

    @BeforeEach
    void setUp() {
        school = new School();
        school.setId(1L);
        school.setName("Test Academy");

        ExamTerm term = new ExamTerm();
        term.setId(10L);
        term.setName("First Term");

        examSetup = new ExamSetup();
        examSetup.setId(100L);
        examSetup.setSchool(school);
        examSetup.setTerm(term);
        examSetup.setName("Mid-Term Evaluation");
        examSetup.setGroupName("Scholastic");
        examSetup.setStatus("ACTIVE");
        examSetup.setEvaluationType("STANDARD");

        grade10 = new SchoolClass();
        grade10.setId(5L);
        grade10.setName("Grade 10");

        sectionA = new Section();
        sectionA.setId(15L);
        sectionA.setName("Section A");
        sectionA.setSchoolClass(grade10);

        mathSubject = new Subject();
        mathSubject.setId(201L);
        mathSubject.setName("Mathematics");
        mathSubject.setCode("MAT-101");
        mathSubject.setType("THEORY");

        csElectiveSubject = new Subject();
        csElectiveSubject.setId(202L);
        csElectiveSubject.setName("Computer Science");
        csElectiveSubject.setCode("CS-101");
        csElectiveSubject.setType("PRACTICAL");

        student1 = new Student();
        student1.setId(1001L);
        student1.setFirstName("Aarav");
        student1.setLastName("Patel");
        student1.setAdmissionNo("ADM-001");
        student1.setSchoolClass(grade10);
        student1.setSectionId(15L);

        student2 = new Student();
        student2.setId(1002L);
        student2.setFirstName("Diya");
        student2.setLastName("Sharma");
        student2.setAdmissionNo("ADM-002");
        student2.setSchoolClass(grade10);
        student2.setSectionId(15L);
    }

    @Test
    void syncApplicabilities_autoDiscoversAllottedSubjectsAndEnrollsStudentsScopedToElectives() {
        when(authContextService.resolveSchoolId(1L)).thenReturn(1L);
        when(examSetupRepository.findByIdAndSchoolId(100L, 1L)).thenReturn(Optional.of(examSetup));
        when(examApplicabilityRepository.findBySchoolIdAndExamSetupId(1L, 100L)).thenReturn(List.of());
        when(schoolClassRepository.findById(5L)).thenReturn(Optional.of(grade10));
        when(sectionRepository.findById(15L)).thenReturn(Optional.of(sectionA));
        when(examApplicabilityRepository.save(any(ExamApplicability.class))).thenAnswer(inv -> {
            ExamApplicability app = inv.getArgument(0);
            app.setId(50L);
            return app;
        });

        ClassSubjectAssignment coreMathAssignment = new ClassSubjectAssignment();
        coreMathAssignment.setSchool(school);
        coreMathAssignment.setSchoolClass(grade10);
        coreMathAssignment.setSection(sectionA);
        coreMathAssignment.setSubject(mathSubject);
        coreMathAssignment.setSubjectType("CORE");
        coreMathAssignment.setStatus("ACTIVE");

        ClassSubjectAssignment electiveCsAssignment = new ClassSubjectAssignment();
        electiveCsAssignment.setSchool(school);
        electiveCsAssignment.setSchoolClass(grade10);
        electiveCsAssignment.setSection(sectionA);
        electiveCsAssignment.setSubject(csElectiveSubject);
        electiveCsAssignment.setSubjectType("ELECTIVE");
        electiveCsAssignment.setStatus("ACTIVE");

        when(classSubjectAssignmentRepository.findBySchoolId(1L))
                .thenReturn(List.of(coreMathAssignment, electiveCsAssignment));
        when(examSubjectConfigRepository.findBySchoolIdAndExamSetupId(1L, 100L))
                .thenReturn(List.of());
        when(examSubjectConfigRepository.save(any(ExamSubjectConfig.class))).thenAnswer(inv -> {
            ExamSubjectConfig cfg = inv.getArgument(0);
            cfg.setId(cfg.getSubject().getId() + 1000L);
            return cfg;
        });

        when(studentRepository.findBySchoolIdAndSchoolClassIdAndSectionId(1L, 5L, 15L))
                .thenReturn(List.of(student1, student2));
        when(examStudentEligibilityRepository.findBySchoolIdAndExamSetupId(1L, 100L))
                .thenReturn(List.of());

        // Student 1 is enrolled in CS elective; Student 2 is only enrolled in another elective (not CS)
        StudentSubjectEnrollment s1CsEnrollment = new StudentSubjectEnrollment();
        s1CsEnrollment.setStudent(student1);
        s1CsEnrollment.setSubject(csElectiveSubject);
        s1CsEnrollment.setStatus("CONFIRMED");
        when(studentSubjectEnrollmentRepository.findBySchoolIdAndStudentId(1L, 1001L))
                .thenReturn(List.of(s1CsEnrollment));

        Subject hindiElective = new Subject();
        hindiElective.setId(203L);
        StudentSubjectEnrollment s2OtherEnrollment = new StudentSubjectEnrollment();
        s2OtherEnrollment.setStudent(student2);
        s2OtherEnrollment.setSubject(hindiElective);
        s2OtherEnrollment.setStatus("CONFIRMED");
        when(studentSubjectEnrollmentRepository.findBySchoolIdAndStudentId(1L, 1002L))
                .thenReturn(List.of(s2OtherEnrollment));

        when(examStudentEligibilityRepository.save(any(ExamStudentEligibility.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        ExamApplicabilitySyncRequest.ClassSectionPair pair = new ExamApplicabilitySyncRequest.ClassSectionPair();
        pair.setClassId(5L);
        pair.setSectionId(15L);
        ExamApplicabilitySyncRequest req = new ExamApplicabilitySyncRequest();
        req.setApplicabilities(List.of(pair));

        examWorkspaceService.syncApplicabilities(1L, 100L, req);

        // Verify both allotted subjects (Math CORE + CS ELECTIVE) were automatically created in ExamSubjectConfig
        ArgumentCaptor<ExamSubjectConfig> configCaptor = ArgumentCaptor.forClass(ExamSubjectConfig.class);
        verify(examSubjectConfigRepository, times(2)).save(configCaptor.capture());
        List<ExamSubjectConfig> savedConfigs = configCaptor.getAllValues();
        assertEquals(2, savedConfigs.size());
        assertEquals(new BigDecimal("100.00"), savedConfigs.get(0).getMaxMarks());
        assertEquals(new BigDecimal("35.00"), savedConfigs.get(0).getPassingMarks());

        // Verify Student 1 got 2 eligibilities (Math + CS) and Student 2 got 1 eligibility (Math only) = 3 total
        ArgumentCaptor<ExamStudentEligibility> eligCaptor = ArgumentCaptor.forClass(ExamStudentEligibility.class);
        verify(examStudentEligibilityRepository, times(3)).save(eligCaptor.capture());
    }

    @Test
    void getOverview_computesDistinctEligibleStudentCount() {
        when(authContextService.resolveSchoolId(1L)).thenReturn(1L);
        when(examSetupRepository.findByIdAndSchoolId(100L, 1L)).thenReturn(Optional.of(examSetup));

        ExamSubjectConfig mathCfg = new ExamSubjectConfig();
        mathCfg.setId(301L);
        mathCfg.setSubject(mathSubject);

        when(examSubjectConfigRepository.findBySchoolIdAndExamSetupId(1L, 100L)).thenReturn(List.of(mathCfg));
        when(examApplicabilityRepository.countBySchoolIdAndExamSetupId(1L, 100L)).thenReturn(2);
        when(examScheduleRepository.countBySchoolIdAndExamSetupId(1L, 100L)).thenReturn(1);

        ExamStudentEligibility e1 = new ExamStudentEligibility();
        e1.setStudent(student1);
        e1.setStatus("ELIGIBLE");

        ExamStudentEligibility e2 = new ExamStudentEligibility();
        e2.setStudent(student2);
        e2.setStatus("EXEMPTED");

        when(examStudentEligibilityRepository.findBySchoolIdAndExamSetupId(1L, 100L))
                .thenReturn(List.of(e1, e2));

        ExamWorkspaceOverviewResponse overview = examWorkspaceService.getOverview(1L, 100L);

        assertEquals(100L, overview.getId());
        assertEquals(1, overview.getSubjectCount());
        assertEquals(2, overview.getClassCount());
        assertEquals(1, overview.getStudentCount());
        assertEquals(1, overview.getScheduleCount());
    }

    @Test
    void getSubjects_enrichesSubjectTypeAndAssignedClassesFromAcademicSOT() {
        when(authContextService.resolveSchoolId(1L)).thenReturn(1L);

        ExamApplicability app = new ExamApplicability();
        app.setId(50L);
        app.setSchoolClass(grade10);
        app.setSection(sectionA);
        when(examApplicabilityRepository.findBySchoolIdAndExamSetupId(1L, 100L)).thenReturn(List.of(app));

        ExamSubjectConfig mathCfg = new ExamSubjectConfig();
        mathCfg.setId(301L);
        mathCfg.setSubject(mathSubject);
        mathCfg.setMaxMarks(new BigDecimal("100.00"));
        mathCfg.setPassingMarks(new BigDecimal("35.00"));
        when(examSubjectConfigRepository.findBySchoolIdAndExamSetupId(1L, 100L)).thenReturn(List.of(mathCfg));

        ClassSubjectAssignment csa = new ClassSubjectAssignment();
        csa.setSchoolClass(grade10);
        csa.setSection(sectionA);
        csa.setSubject(mathSubject);
        csa.setSubjectType("CORE");
        when(classSubjectAssignmentRepository.findBySchoolId(1L)).thenReturn(List.of(csa));

        List<ExamSubjectConfigDto> subjects = examWorkspaceService.getSubjects(1L, 100L);

        assertEquals(1, subjects.size());
        assertEquals("Mathematics", subjects.get(0).getSubjectName());
        assertEquals("CORE", subjects.get(0).getSubjectType());
        assertEquals(List.of("Grade 10 (Section A)"), subjects.get(0).getAssignedClasses());
    }
}
