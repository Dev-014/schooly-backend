package com.school.erp.service.exam;

import com.school.erp.dto.exam.AdmitCardRosterItemResponse;
import com.school.erp.dto.exam.AdmitCardStatsResponse;
import com.school.erp.dto.exam.GenerateAdmitCardRequest;
import com.school.erp.entity.superadmin.School;
import com.school.erp.entity.academic.SchoolClass;
import com.school.erp.entity.academic.Section;
import com.school.erp.entity.student.Student;
import com.school.erp.entity.exam.ExamAdmitCard;
import com.school.erp.entity.exam.ExamSetup;
import com.school.erp.repository.superadmin.SchoolRepository;
import com.school.erp.repository.academic.SectionRepository;
import com.school.erp.repository.student.StudentRepository;
import com.school.erp.repository.exam.ExamAdmitCardRepository;
import com.school.erp.repository.exam.ExamScheduleRepository;
import com.school.erp.repository.exam.ExamSetupRepository;
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

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExamAdmitCardServiceTest {

    @Mock
    private AuthContextService authContextService;

    @Mock
    private ExamAdmitCardRepository examAdmitCardRepository;

    @Mock
    private ExamSetupRepository examSetupRepository;

    @Mock
    private ExamScheduleRepository examScheduleRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private SchoolRepository schoolRepository;

    @Mock
    private SectionRepository sectionRepository;

    @InjectMocks
    private ExamAdmitCardService examAdmitCardService;

    private School school;
    private SchoolClass schoolClass10;
    private Section sectionA;
    private ExamSetup examSetup;
    private Student student1;
    private Student student2;

    @BeforeEach
    void setUp() {
        school = new School();
        school.setId(4L);
        school.setName("Legal Academy");

        schoolClass10 = new SchoolClass();
        schoolClass10.setId(10L);
        schoolClass10.setName("Grade 10");
        schoolClass10.setSchool(school);

        sectionA = new Section();
        sectionA.setId(6L);
        sectionA.setName("Section A");

        examSetup = new ExamSetup();
        examSetup.setId(4L);
        examSetup.setName("Unit test");
        examSetup.setSchool(school);

        student1 = new Student();
        student1.setId(16L);
        student1.setName("Sohan Prajapat");
        student1.setAdmissionNo("ADM-16");
        student1.setRollNumber("56");
        student1.setSchool(school);
        student1.setSchoolClass(schoolClass10);
        student1.setSectionId(6L);

        student2 = new Student();
        student2.setId(12L);
        student2.setName("Kiyansh Panchal");
        student2.setAdmissionNo("ADM-12");
        student2.setRollNumber("20");
        student2.setSchool(school);
        student2.setSchoolClass(schoolClass10);
        student2.setSectionId(6L);
    }

    @Test
    void filterAdmitCards_WithExamSetupAndNoClass_SyncsEntireSchoolRoster() {
        when(authContextService.resolveSchoolId(4L)).thenReturn(4L);
        when(examSetupRepository.findByIdAndSchoolId(4L, 4L)).thenReturn(Optional.of(examSetup));
        when(studentRepository.findBySchoolId(4L)).thenReturn(List.of(student1, student2));
        when(examAdmitCardRepository.findBySchoolIdAndExamSetupIdAndStudentId(4L, 4L, 16L)).thenReturn(Optional.empty());
        when(examAdmitCardRepository.findBySchoolIdAndExamSetupIdAndStudentId(4L, 4L, 12L)).thenReturn(Optional.empty());
        when(sectionRepository.findById(6L)).thenReturn(Optional.of(sectionA));

        ExamAdmitCard card1 = new ExamAdmitCard();
        card1.setId(1L);
        card1.setSchool(school);
        card1.setExamSetup(examSetup);
        card1.setStudent(student1);
        card1.setSchoolClass(schoolClass10);
        card1.setSection(sectionA);
        card1.setRollNumber("56");
        card1.setCardNumber("AC-4-4-ADM-16");
        card1.setStatus("PENDING");

        Page<ExamAdmitCard> mockPage = new PageImpl<>(List.of(card1), PageRequest.of(0, 10), 1);
        when(examAdmitCardRepository.filterAdmitCards(eq(4L), eq(4L), isNull(), isNull(), isNull(), any()))
                .thenReturn(mockPage);

        Page<AdmitCardRosterItemResponse> result = examAdmitCardService.filterAdmitCards(
                4L, 4L, null, null, null, PageRequest.of(0, 10));

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Sohan Prajapat", result.getContent().get(0).getStudentName());
        verify(examAdmitCardRepository, times(2)).save(any(ExamAdmitCard.class));
    }

    @Test
    void filterAdmitCards_WithClassId_SyncsClassRosterOnly() {
        when(authContextService.resolveSchoolId(4L)).thenReturn(4L);
        when(examSetupRepository.findByIdAndSchoolId(4L, 4L)).thenReturn(Optional.of(examSetup));
        when(studentRepository.findBySchoolIdAndSchoolClassId(4L, 10L)).thenReturn(List.of(student1));
        when(examAdmitCardRepository.findBySchoolIdAndExamSetupIdAndStudentId(4L, 4L, 16L)).thenReturn(Optional.empty());
        when(sectionRepository.findById(6L)).thenReturn(Optional.of(sectionA));

        ExamAdmitCard card1 = new ExamAdmitCard();
        card1.setId(1L);
        card1.setSchool(school);
        card1.setExamSetup(examSetup);
        card1.setStudent(student1);
        card1.setSchoolClass(schoolClass10);
        card1.setSection(sectionA);
        card1.setRollNumber("56");
        card1.setCardNumber("AC-4-4-ADM-16");
        card1.setStatus("PENDING");

        Page<ExamAdmitCard> mockPage = new PageImpl<>(List.of(card1), PageRequest.of(0, 10), 1);
        when(examAdmitCardRepository.filterAdmitCards(eq(4L), eq(4L), eq(10L), isNull(), isNull(), any()))
                .thenReturn(mockPage);

        Page<AdmitCardRosterItemResponse> result = examAdmitCardService.filterAdmitCards(
                4L, 4L, 10L, null, null, PageRequest.of(0, 10));

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(studentRepository).findBySchoolIdAndSchoolClassId(4L, 10L);
        verify(studentRepository, never()).findBySchoolId(anyLong());
    }

    @Test
    void getStats_WithClassId_ReturnsClassFilteredCounts() {
        when(authContextService.resolveSchoolId(4L)).thenReturn(4L);
        when(examSetupRepository.findByIdAndSchoolId(4L, 4L)).thenReturn(Optional.of(examSetup));
        when(studentRepository.findBySchoolIdAndSchoolClassId(4L, 10L)).thenReturn(List.of(student1));

        when(examAdmitCardRepository.countBySchoolIdAndExamSetupIdAndSchoolClassId(4L, 4L, 10L)).thenReturn(1L);
        when(examAdmitCardRepository.countBySchoolIdAndExamSetupIdAndSchoolClassIdAndStatus(4L, 4L, 10L, "GENERATED")).thenReturn(1L);
        when(examAdmitCardRepository.countBySchoolIdAndExamSetupIdAndSchoolClassIdAndStatus(4L, 4L, 10L, "PENDING")).thenReturn(0L);
        when(examAdmitCardRepository.countBySchoolIdAndExamSetupIdAndSchoolClassIdAndStatus(4L, 4L, 10L, "RELEASED")).thenReturn(0L);

        AdmitCardStatsResponse stats = examAdmitCardService.getStats(4L, 4L, 10L);

        assertNotNull(stats);
        assertEquals(1L, stats.getTotalStudents());
        assertEquals(1L, stats.getGeneratedCount());
        assertEquals(0L, stats.getPendingCount());
        assertEquals(0L, stats.getReleasedCount());
    }

    @Test
    void generateAdmitCards_GeneratesWithUniqueCardNumber() {
        when(authContextService.resolveSchoolId(4L)).thenReturn(4L);
        when(examSetupRepository.findByIdAndSchoolId(4L, 4L)).thenReturn(Optional.of(examSetup));
        when(studentRepository.findAllById(List.of(16L))).thenReturn(List.of(student1));
        when(schoolRepository.findById(4L)).thenReturn(Optional.of(school));
        when(examAdmitCardRepository.findBySchoolIdAndExamSetupIdAndStudentId(4L, 4L, 16L)).thenReturn(Optional.empty());
        when(sectionRepository.findById(6L)).thenReturn(Optional.of(sectionA));
        when(examAdmitCardRepository.save(any(ExamAdmitCard.class))).thenAnswer(inv -> inv.getArgument(0));

        GenerateAdmitCardRequest req = new GenerateAdmitCardRequest();
        req.setExamSetupId(4L);
        req.setStudentIds(List.of(16L));

        List<AdmitCardRosterItemResponse> responses = examAdmitCardService.generateAdmitCards(4L, req);

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("GENERATED", responses.get(0).getStatus());
        assertEquals("AC-4-4-ADM-16", responses.get(0).getCardNumber());
    }

    @Test
    void releaseAllCards_DelegatesToRepository() {
        when(authContextService.resolveSchoolId(4L)).thenReturn(4L);
        when(examSetupRepository.findByIdAndSchoolId(4L, 4L)).thenReturn(Optional.of(examSetup));
        when(examAdmitCardRepository.releaseAllGeneratedCards(eq(4L), eq(4L), any())).thenReturn(5);

        int released = examAdmitCardService.releaseAllCards(4L, 4L);

        assertEquals(5, released);
    }
}
