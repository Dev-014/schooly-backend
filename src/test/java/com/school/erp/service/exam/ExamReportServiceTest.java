package com.school.erp.service.exam;

import com.school.erp.dto.exam.ExamReportAnalyticsResponse;
import com.school.erp.dto.exam.ExamReportMetricsResponse;
import com.school.erp.entity.academic.SchoolClass;
import com.school.erp.entity.academic.Subject;
import com.school.erp.entity.exam.ExamMark;
import com.school.erp.entity.exam.ExamSetup;
import com.school.erp.entity.exam.ExamSubjectConfig;
import com.school.erp.entity.exam.ExamTerm;
import com.school.erp.entity.student.Student;
import com.school.erp.entity.superadmin.School;
import com.school.erp.repository.academic.SchoolClassRepository;
import com.school.erp.repository.exam.ExamMarkRepository;
import com.school.erp.repository.exam.ExamReportCardRepository;
import com.school.erp.repository.student.StudentRepository;
import com.school.erp.security.AuthContextService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExamReportServiceTest {

    @Mock
    private AuthContextService authContextService;

    @Mock
    private ExamReportCardRepository examReportCardRepository;

    @Mock
    private ExamMarkRepository examMarkRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private SchoolClassRepository schoolClassRepository;

    @InjectMocks
    private ExamReportService examReportService;

    private School school;
    private SchoolClass schoolClass;
    private Student student;
    private ExamSetup setup;
    private ExamTerm term;
    private Subject subject;
    private ExamSubjectConfig config;
    private ExamMark mark;

    @BeforeEach
    void setUp() {
        school = new School();
        school.setId(1000L);

        schoolClass = new SchoolClass();
        schoolClass.setId(2L);

        student = new Student();
        student.setId(5L);
        student.setName("Atul Yadav");
        student.setSchool(school);
        student.setSchoolClass(schoolClass);

        term = new ExamTerm();
        term.setId(1L);
        term.setName("Term 1");

        setup = new ExamSetup();
        setup.setId(4L);
        setup.setTerm(term);

        subject = new Subject();
        subject.setId(10L);
        subject.setName("English");

        config = new ExamSubjectConfig();
        config.setId(1L);
        config.setMaxMarks(new BigDecimal("100.00"));

        mark = new ExamMark();
        mark.setId(1L);
        mark.setSchool(school);
        mark.setStudent(student);
        mark.setSchoolClass(schoolClass);
        mark.setExamSetup(setup);
        mark.setSubject(subject);
        mark.setExamSubjectConfig(config);
        mark.setMarksObtained(new BigDecimal("80.00"));
    }

    @Test
    void getMetrics_computesRealClassAverageAndHighestScore() {
        when(authContextService.resolveSchoolId(1000L)).thenReturn(1000L);
        when(studentRepository.findBySchoolIdAndSchoolClassIdAndSectionId(1000L, 2L, 5L))
                .thenReturn(List.of(student));
        when(examMarkRepository.findBySchoolIdAndExamSetupIdAndStudentIdIn(1000L, 4L, List.of(5L)))
                .thenReturn(List.of(mark));

        ExamReportMetricsResponse metrics = examReportService.getMetrics(
                1000L, 1L, 2L, 5L, null, 4L);

        assertNotNull(metrics);
        assertEquals(1, metrics.getTotalStudents());
        assertEquals(new BigDecimal("80.00"), metrics.getClassAverage());
        assertEquals(new BigDecimal("80.00"), metrics.getHighestScore());
        assertEquals("Atul Yadav", metrics.getHighestScorerName());
        assertEquals(new BigDecimal("100.00"), metrics.getPassPercentage());
    }

    @Test
    void getAnalytics_computesSubjectBreakdownAndTermPerformance() {
        when(authContextService.resolveSchoolId(1000L)).thenReturn(1000L);
        when(studentRepository.findBySchoolIdAndSchoolClassIdAndSectionId(1000L, 2L, 5L))
                .thenReturn(List.of(student));
        when(examMarkRepository.findBySchoolIdAndExamSetupIdAndStudentIdIn(1000L, 4L, List.of(5L)))
                .thenReturn(List.of(mark));

        ExamReportAnalyticsResponse analytics = examReportService.getAnalytics(
                1000L, "2026-2027", 1L, 2L, 5L, 4L);

        assertNotNull(analytics);
        assertEquals(new BigDecimal("80.00"), analytics.getAvgClassPerformance());
        assertFalse(analytics.getSubjectBreakdown().isEmpty());
        assertEquals("English", analytics.getSubjectBreakdown().get(0).getSubjectName());
        assertFalse(analytics.getTermReports().isEmpty());
        assertEquals("Term 1", analytics.getTermReports().get(0).getTermName());
    }
}
