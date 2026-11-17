package com.school.erp.service;

import com.school.erp.entity.academic.SchoolClass;
import com.school.erp.entity.academic.Section;
import com.school.erp.entity.student.Student;
import com.school.erp.entity.student.StudentParent;
import com.school.erp.entity.superadmin.School;
import com.school.erp.repository.academic.SectionRepository;
import com.school.erp.repository.attendance.AttendanceRepository;
import com.school.erp.repository.library.LibraryCirculationRepository;
import com.school.erp.repository.library.LibraryMemberRepository;
import com.school.erp.repository.student.StudentParentRepository;
import com.school.erp.repository.student.StudentRepository;
import com.school.erp.service.finance.FeeDueService;
import com.school.erp.service.student.StudentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentProfileServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private SectionRepository sectionRepository;

    @Mock
    private FeeDueService feeDueService;

    @Mock
    private StudentService studentService;

    @Mock
    private AttendanceRepository attendanceRepository;

    @Mock
    private LibraryCirculationRepository circulationRepository;

    @Mock
    private LibraryMemberRepository memberRepository;

    @Mock
    private StudentParentRepository studentParentRepository;

    @InjectMocks
    private StudentProfileService studentProfileService;

    private School school;
    private SchoolClass class10;
    private Section sectionC;
    private Student palak;

    @BeforeEach
    void setUp() {
        school = new School();
        school.setId(1003L);
        school.setName("Test School");

        class10 = new SchoolClass();
        class10.setId(21L);
        class10.setName("Class 10");

        sectionC = new Section();
        sectionC.setId(37L);
        sectionC.setName("Section C");

        palak = new Student();
        palak.setId(34L);
        palak.setFirstName("Palak");
        palak.setLastName("Patra");
        palak.setSchool(school);
        palak.setSchoolClass(class10);
        palak.setSectionId(37L);
    }

    @Test
    void resolveStudent_whenParentUserIdProvided_resolvesLinkedChild() {
        Long parentUserId = 81L;
        Long schoolId = 1003L;

        // User is not a student
        when(studentRepository.findByUserId(parentUserId)).thenReturn(List.of());

        // User is a parent linked to Palak
        StudentParent studentParent = mock(StudentParent.class);
        when(studentParent.getStudent()).thenReturn(palak);
        when(studentParentRepository.findByIdParentUserIdAndStudentSchoolId(parentUserId, schoolId))
                .thenReturn(List.of(studentParent));

        Student result = studentProfileService.resolveStudent(null, schoolId, parentUserId);

        assertNotNull(result);
        assertEquals(34L, result.getId());
        assertEquals("Palak", result.getFirstName());
        assertEquals("Patra", result.getLastName());
        assertEquals(21L, result.getSchoolClass().getId());
        assertEquals(37L, result.getSectionId());
    }

    @Test
    void resolveStudent_whenStudentIdProvided_resolvesDirectly() {
        when(studentRepository.findByIdAndSchoolId(34L, 1003L)).thenReturn(Optional.of(palak));

        Student result = studentProfileService.resolveStudent(34L, 1003L, 81L);

        assertNotNull(result);
        assertEquals(34L, result.getId());
        assertEquals("Palak", result.getFirstName());
    }
}

