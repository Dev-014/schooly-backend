package com.school.erp.service.exam;

import com.school.erp.dto.exam.ExamAttendanceItemResponse;
import com.school.erp.entity.academic.SchoolClass;
import com.school.erp.entity.academic.Section;
import com.school.erp.entity.exam.ExamAttendance;
import com.school.erp.entity.exam.ExamSchedule;
import com.school.erp.entity.exam.ExamSetup;
import com.school.erp.entity.exam.ExamTerm;
import com.school.erp.entity.student.Student;
import com.school.erp.entity.superadmin.School;
import com.school.erp.repository.academic.SchoolClassRepository;
import com.school.erp.repository.academic.SectionRepository;
import com.school.erp.repository.exam.ExamAttendanceRepository;
import com.school.erp.repository.exam.ExamScheduleRepository;
import com.school.erp.repository.exam.ExamSetupRepository;
import com.school.erp.repository.exam.ExamTermRepository;
import com.school.erp.repository.student.StudentRepository;
import com.school.erp.repository.superadmin.SchoolRepository;
import com.school.erp.security.AuthContextService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExamAttendanceServiceTest {

    @Mock
    private AuthContextService authContextService;

    @Mock
    private ExamAttendanceRepository examAttendanceRepository;

    @Mock
    private ExamScheduleRepository examScheduleRepository;

    @Mock
    private ExamTermRepository examTermRepository;

    @Mock
    private ExamSetupRepository examSetupRepository;

    @Mock
    private SchoolRepository schoolRepository;

    @Mock
    private SchoolClassRepository schoolClassRepository;

    @Mock
    private SectionRepository sectionRepository;

    @Mock
    private StudentRepository studentRepository;

    @InjectMocks
    private ExamAttendanceService examAttendanceService;

    private School school;
    private ExamTerm term;
    private ExamSetup setup;
    private SchoolClass schoolClass;
    private Section section;
    private Student student;
    private ExamSchedule schedule;

    @BeforeEach
    void setUp() {
        school = new School();
        school.setId(1000L);

        term = new ExamTerm();
        term.setId(1L);
        term.setSchool(school);

        setup = new ExamSetup();
        setup.setId(4L);
        setup.setSchool(school);
        setup.setTerm(term);

        schoolClass = new SchoolClass();
        schoolClass.setId(2L);

        section = new Section();
        section.setId(5L);

        student = new Student();
        student.setId(4L);
        student.setSchool(school);
        student.setSchoolClass(schoolClass);
        student.setSectionId(5L);
        student.setName("Ajay Purohit");
        student.setRollNumber("1");
        student.setAdmissionNo("AC-2026-378");
        student.setStatus("ACTIVE");

        schedule = new ExamSchedule();
        schedule.setId(4L);
        schedule.setSchool(school);
        schedule.setExamSetup(setup);
    }

    @Test
    void filterAttendance_resolvesTermFromSetupAndSyncsRosterWhenTermIdIsNull() {
        when(authContextService.resolveSchoolId(1000L)).thenReturn(1000L);
        when(examSetupRepository.findByIdAndSchoolId(4L, 1000L)).thenReturn(Optional.of(setup));
        when(studentRepository.findBySchoolIdAndSchoolClassIdAndSectionId(1000L, 2L, 5L))
                .thenReturn(List.of(student));
        when(examScheduleRepository.findSchedulesForAttendanceSummary(1000L, 1L, 4L, 2L, 5L, 10L))
                .thenReturn(List.of(schedule));
        when(examAttendanceRepository.findBySchoolIdAndTermIdAndExamScheduleIdAndStudentIdIn(1000L, 1L, 4L, List.of(4L)))
                .thenReturn(List.of());
        when(examTermRepository.findByIdAndSchoolId(1L, 1000L)).thenReturn(Optional.of(term));

        ExamAttendance createdAttendance = new ExamAttendance();
        createdAttendance.setId(101L);
        createdAttendance.setSchool(school);
        createdAttendance.setTerm(term);
        createdAttendance.setStudent(student);
        createdAttendance.setSchoolClass(schoolClass);
        createdAttendance.setSection(section);
        createdAttendance.setExamSchedule(schedule);
        createdAttendance.setAttendanceStatus("PRESENT");

        when(examAttendanceRepository.saveAll(any())).thenReturn(List.of(createdAttendance));
        when(examAttendanceRepository.filterAttendance(
                eq(1000L), eq(1L), eq(2L), eq(5L), eq(4L), eq(10L), isNull(), isNull(), any(Pageable.class)
        )).thenReturn(new PageImpl<>(List.of(createdAttendance), PageRequest.of(0, 10), 1));

        Page<ExamAttendanceItemResponse> result = examAttendanceService.filterAttendance(
                1000L, null, 2L, 5L, 4L, 10L, null, null, PageRequest.of(0, 10)
        );

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        ExamAttendanceItemResponse item = result.getContent().get(0);
        assertEquals(4L, item.getStudentId());
        assertEquals(101L, item.getId());
        assertEquals("PRESENT", item.getAttendanceStatus());
        verify(examAttendanceRepository, atLeastOnce()).saveAll(any());
    }
}
