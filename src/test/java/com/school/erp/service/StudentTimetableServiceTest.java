package com.school.erp.service;

import com.school.erp.dto.academic.TimetableEntryResponse;
import com.school.erp.dto.academic.TimetablePeriodResponse;
import com.school.erp.dto.student.StudentTimetableResponse;
import com.school.erp.entity.AcademicYear;
import com.school.erp.entity.School;
import com.school.erp.entity.SchoolClass;
import com.school.erp.entity.Section;
import com.school.erp.entity.Student;
import com.school.erp.repository.AcademicYearRepository;
import com.school.erp.repository.SectionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StudentTimetableServiceTest {

    @Mock
    private StudentProfileService studentProfileService;

    @Mock
    private AcademicService academicService;

    @Mock
    private SectionRepository sectionRepository;

    @Mock
    private AcademicYearRepository academicYearRepository;

    @InjectMocks
    private StudentTimetableService studentTimetableService;

    private Student student;
    private School school;
    private SchoolClass schoolClass;

    @BeforeEach
    void setUp() {
        school = new School();
        school.setId(4L);
        school.setName("Greenwood High");

        schoolClass = new SchoolClass();
        schoolClass.setId(10L);
        schoolClass.setName("Grade 12");

        student = new Student();
        student.setId(16L);
        student.setFirstName("Sohan");
        student.setLastName("Prajapat");
        student.setAdmissionNo("AC-2026-884");
        student.setRollNumber("14");
        student.setSchool(school);
        student.setSchoolClass(schoolClass);
        student.setSectionId(12L);
        student.setAcademicYearId(1L);
    }

    @Test
    void getStudentTimetable_ShouldReturnOnlyRealAssignedEntriesWithoutInjectingDummyDays() {
        when(studentProfileService.resolveStudent(eq(16L), eq(4L), any())).thenReturn(student);

        Section section = new Section();
        section.setId(12L);
        section.setName("Section A");
        when(sectionRepository.findByIdAndSchoolId(12L, 4L)).thenReturn(Optional.of(section));

        AcademicYear year = new AcademicYear();
        year.setId(1L);
        year.setName("2026-2027");
        when(academicYearRepository.findByIdAndSchoolId(1L, 4L)).thenReturn(Optional.of(year));

        List<TimetablePeriodResponse> periods = List.of(
                new TimetablePeriodResponse(9L, 4L, 1, "Period 1", "10:00", "11:00", false),
                new TimetablePeriodResponse(10L, 4L, 2, "Period 2", "11:00", "12:00", false)
        );
        when(academicService.getTimetablePeriods(4L)).thenReturn(periods);

        // Only Monday and Tuesday have assigned classes in database
        List<TimetableEntryResponse> dbEntries = List.of(
                new TimetableEntryResponse(
                        13L, 4L, 10L, "Grade 12", 12L, "Section A", 1L, "2026-2027",
                        "MONDAY", 9L, 1, "Period 1", "10:00", "11:00", false,
                        10L, "Maths", "MTH-101", 2L, "Amit Singh", "Room 302", "ACTIVE"
                ),
                new TimetableEntryResponse(
                        14L, 4L, 10L, "Grade 12", 12L, "Section A", 1L, "2026-2027",
                        "MONDAY", 10L, 2, "Period 2", "11:00", "12:00", false,
                        11L, "English", "ENG-101", 3L, "Chanchal Singh", "Room 303", "ACTIVE"
                ),
                new TimetableEntryResponse(
                        15L, 4L, 10L, "Grade 12", 12L, "Section A", 1L, "2026-2027",
                        "TUESDAY", 9L, 1, "Period 1", "10:00", "11:00", false,
                        12L, "Computer Science", "CS-101", 4L, "Kapil Dua", "Lab 1", "ACTIVE"
                ),
                new TimetableEntryResponse(
                        16L, 4L, 10L, "Grade 12", 12L, "Section A", 1L, "2026-2027",
                        "TUESDAY", 10L, 2, "Period 2", "11:00", "12:00", false,
                        11L, "English", "ENG-101", 10L, "Nauman Khan", "Room 303", "ACTIVE"
                )
        );

        when(academicService.getTimetableGrid(4L, 10L, 12L, null, 1L)).thenReturn(dbEntries);

        StudentTimetableResponse response = studentTimetableService.getStudentTimetable(16L, 4L, null, null);

        assertNotNull(response);
        assertEquals(16L, response.studentId());
        assertEquals("Sohan Prajapat", response.studentName());
        assertEquals("Grade 12", response.className());
        assertEquals("Section A", response.sectionName());

        // Exactly 4 entries should be returned, ONLY Monday & Tuesday!
        assertEquals(4, response.entries().size());
        assertTrue(response.entries().stream().allMatch(e ->
                e.dayOfWeek().equalsIgnoreCase("MONDAY") || e.dayOfWeek().equalsIgnoreCase("TUESDAY")
        ));

        // Wednesday, Thursday, Friday must NOT be artificially generated
        assertFalse(response.entries().stream().anyMatch(e -> e.dayOfWeek().equalsIgnoreCase("WEDNESDAY")));
        assertFalse(response.entries().stream().anyMatch(e -> e.dayOfWeek().equalsIgnoreCase("THURSDAY")));
        assertFalse(response.entries().stream().anyMatch(e -> e.dayOfWeek().equalsIgnoreCase("FRIDAY")));
    }

    @Test
    void getStudentTimetable_WhenNoDbEntries_ShouldReturnEmptyListWithoutFabricatingClasses() {
        when(studentProfileService.resolveStudent(eq(16L), eq(4L), any())).thenReturn(student);
        when(academicService.getTimetableGrid(eq(4L), eq(10L), eq(12L), any(), any())).thenReturn(List.of());
        when(academicService.getTimetableGrid(eq(4L), eq(10L), eq(null), any(), any())).thenReturn(List.of());

        StudentTimetableResponse response = studentTimetableService.getStudentTimetable(16L, 4L, null, null);

        assertNotNull(response);
        assertNotNull(response.entries());
        assertTrue(response.entries().isEmpty(), "Entries should be empty when no timetable is configured");
    }
}
