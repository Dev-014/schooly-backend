package com.school.erp.service.exam;

import com.school.erp.dto.exam.ExamScheduleRequest;
import com.school.erp.dto.exam.ExamScheduleResponse;
import com.school.erp.entity.superadmin.School;
import com.school.erp.entity.academic.SchoolClass;
import com.school.erp.entity.academic.Subject;
import com.school.erp.entity.exam.ExamSchedule;
import com.school.erp.entity.exam.ExamSetup;
import com.school.erp.exception.ResourceNotFoundException;
import com.school.erp.repository.academic.SchoolClassRepository;
import com.school.erp.repository.superadmin.SchoolRepository;
import com.school.erp.repository.academic.SectionRepository;
import com.school.erp.repository.academic.SubjectRepository;
import com.school.erp.repository.exam.ExamScheduleRepository;
import com.school.erp.repository.exam.ExamSetupRepository;
import com.school.erp.security.AuthContextService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExamScheduleServiceTest {

    @Mock
    private AuthContextService authContextService;

    @Mock
    private ExamScheduleRepository examScheduleRepository;

    @Mock
    private ExamSetupRepository examSetupRepository;

    @Mock
    private SchoolRepository schoolRepository;

    @Mock
    private SchoolClassRepository schoolClassRepository;

    @Mock
    private SectionRepository sectionRepository;

    @Mock
    private SubjectRepository subjectRepository;

    @InjectMocks
    private ExamScheduleService examScheduleService;

    private School school;
    private SchoolClass schoolClass;
    private ExamSetup examSetup;
    private Subject subject;

    @BeforeEach
    void setUp() {
        school = new School();
        school.setId(4L);
        school.setName("Legal Academy");

        schoolClass = new SchoolClass();
        schoolClass.setId(10L);
        schoolClass.setSchool(school);
        schoolClass.setName("Grade 12");

        examSetup = new ExamSetup();
        examSetup.setId(4L);
        examSetup.setSchool(school);
        examSetup.setName("Unit test (Term 5)");

        subject = new Subject();
        subject.setId(10L);
        subject.setSchool(school);
        subject.setName("Maths");
        subject.setCode("MAT-001");
    }

    @Test
    void createSchedule_WithSubjectId_AutomaticallyMapsSubjectCode() {
        when(authContextService.resolveSchoolId(4L)).thenReturn(4L);
        when(schoolRepository.findById(4L)).thenReturn(Optional.of(school));
        when(examSetupRepository.findByIdAndSchoolId(4L, 4L)).thenReturn(Optional.of(examSetup));
        when(schoolClassRepository.findByIdAndSchoolId(10L, 4L)).thenReturn(Optional.of(schoolClass));
        when(subjectRepository.findByIdAndSchoolId(10L, 4L)).thenReturn(Optional.of(subject));
        when(examScheduleRepository.save(any(ExamSchedule.class))).thenAnswer(invocation -> {
            ExamSchedule s = invocation.getArgument(0);
            s.setId(100L);
            return s;
        });

        ExamScheduleRequest request = new ExamScheduleRequest();
        request.setExamSetupId(4L);
        request.setClassId(10L);
        request.setSubjectId(10L);
        request.setExamDate(LocalDate.of(2026, 9, 20));
        request.setStartTime(LocalTime.of(9, 0));
        request.setEndTime(LocalTime.of(12, 0));
        request.setRoomNumber("Hall 12");
        request.setFullMarks(new BigDecimal("100.00"));
        request.setPassingMarks(new BigDecimal("35.00"));

        ExamScheduleResponse response = examScheduleService.createSchedule(4L, request);

        assertNotNull(response);
        assertEquals(100L, response.getId());
        assertEquals("Maths", response.getSubjectName());
        assertEquals("MAT-001", response.getSubjectCode());
        assertEquals(10L, response.getSubjectId());
    }

    @Test
    void createSchedule_WithSubjectCodeFallback_ResolvesAndMapsSubjectCode() {
        when(authContextService.resolveSchoolId(4L)).thenReturn(4L);
        when(schoolRepository.findById(4L)).thenReturn(Optional.of(school));
        when(examSetupRepository.findByIdAndSchoolId(4L, 4L)).thenReturn(Optional.of(examSetup));
        when(schoolClassRepository.findByIdAndSchoolId(10L, 4L)).thenReturn(Optional.of(schoolClass));
        when(subjectRepository.findBySchoolIdAndCode(4L, "MAT-001")).thenReturn(Optional.of(subject));
        when(examScheduleRepository.save(any(ExamSchedule.class))).thenAnswer(invocation -> {
            ExamSchedule s = invocation.getArgument(0);
            s.setId(101L);
            return s;
        });

        ExamScheduleRequest request = new ExamScheduleRequest();
        request.setExamSetupId(4L);
        request.setClassId(10L);
        request.setSubjectCode("MAT-001");
        request.setExamDate(LocalDate.of(2026, 9, 20));
        request.setStartTime(LocalTime.of(9, 0));
        request.setEndTime(LocalTime.of(12, 0));

        ExamScheduleResponse response = examScheduleService.createSchedule(4L, request);

        assertNotNull(response);
        assertEquals("Maths", response.getSubjectName());
        assertEquals("MAT-001", response.getSubjectCode());
    }

    @Test
    void createSchedule_WithoutClassId_FallsBackToFirstSchoolClass() {
        when(authContextService.resolveSchoolId(4L)).thenReturn(4L);
        when(schoolRepository.findById(4L)).thenReturn(Optional.of(school));
        when(examSetupRepository.findByIdAndSchoolId(4L, 4L)).thenReturn(Optional.of(examSetup));
        when(schoolClassRepository.findBySchoolId(4L)).thenReturn(List.of(schoolClass));
        when(subjectRepository.findByIdAndSchoolId(10L, 4L)).thenReturn(Optional.of(subject));
        when(examScheduleRepository.save(any(ExamSchedule.class))).thenAnswer(invocation -> {
            ExamSchedule s = invocation.getArgument(0);
            s.setId(102L);
            return s;
        });

        ExamScheduleRequest request = new ExamScheduleRequest();
        request.setExamSetupId(4L);
        request.setSubjectId(10L);
        request.setExamDate(LocalDate.of(2026, 9, 20));
        request.setStartTime(LocalTime.of(9, 0));
        request.setEndTime(LocalTime.of(12, 0));

        ExamScheduleResponse response = examScheduleService.createSchedule(4L, request);

        assertNotNull(response);
        assertEquals("Grade 12", response.getClassName());
        assertEquals(10L, response.getClassId());
    }

    @Test
    void createSchedule_WhenSubjectNotFound_ThrowsResourceNotFoundException() {
        when(authContextService.resolveSchoolId(4L)).thenReturn(4L);
        when(schoolRepository.findById(4L)).thenReturn(Optional.of(school));
        when(examSetupRepository.findByIdAndSchoolId(4L, 4L)).thenReturn(Optional.of(examSetup));
        when(schoolClassRepository.findByIdAndSchoolId(10L, 4L)).thenReturn(Optional.of(schoolClass));
        when(subjectRepository.findByIdAndSchoolId(5065L, 4L)).thenReturn(Optional.empty());
        when(subjectRepository.findById(5065L)).thenReturn(Optional.empty());

        ExamScheduleRequest request = new ExamScheduleRequest();
        request.setExamSetupId(4L);
        request.setClassId(10L);
        request.setSubjectId(5065L);
        request.setExamDate(LocalDate.of(2026, 9, 20));
        request.setStartTime(LocalTime.of(9, 0));
        request.setEndTime(LocalTime.of(12, 0));

        assertThrows(ResourceNotFoundException.class, () -> examScheduleService.createSchedule(4L, request));
    }
}
