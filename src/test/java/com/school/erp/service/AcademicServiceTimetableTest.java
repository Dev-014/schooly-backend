package com.school.erp.service;

import com.school.erp.dto.academic.BulkTimetableRequest;
import com.school.erp.dto.academic.TimetableEntryRequest;
import com.school.erp.dto.academic.TimetableEntryResponse;
import com.school.erp.entity.*;
import com.school.erp.exception.ResourceNotFoundException;
import com.school.erp.repository.*;
import com.school.erp.security.AuthContextService;
import com.school.erp.service.EntitlementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AcademicServiceTimetableTest {

    @Mock private TimetableEntryRepository timetableEntryRepository;
    @Mock private TimetablePeriodRepository periodRepository;
    @Mock private SubjectRepository subjectRepository;
    @Mock private StaffRepository staffRepository;
    @Mock private SchoolClassRepository classRepository;
    @Mock private SectionRepository sectionRepository;
    @Mock private AcademicYearRepository academicYearRepository;
    @Mock private SchoolRepository schoolRepository;
    @Mock private AuthContextService authContextService;
    @Mock private EntitlementService entitlementService;

    @InjectMocks
    private AcademicService academicService;

    private School school;
    private SchoolClass schoolClass;
    private AcademicYear academicYear;
    private TimetablePeriod period;

    @BeforeEach
    void setUp() {
        school = new School();
        school.setId(1L);

        schoolClass = new SchoolClass();
        schoolClass.setId(1L);

        academicYear = new AcademicYear();
        academicYear.setId(1L);

        period = new TimetablePeriod();
        period.setId(1L);
        period.setPeriodNumber(1);
        period.setName("Period 1");
        period.setStartTime("08:30");
        period.setEndTime("09:15");
        period.setIsBreak(false);
    }

    @Test
    void saveTimetableGrid_ShouldSaveSuccessfully() {
        TimetableEntryRequest req = new TimetableEntryRequest(1L, 1L, null, 1L, "MONDAY", 1L, null, null, "101");
        BulkTimetableRequest request = new BulkTimetableRequest(1L, 1L, null, 1L, List.of(req));

        when(authContextService.resolveSchoolId(1L)).thenReturn(1L);
        when(schoolRepository.findById(1L)).thenReturn(Optional.of(school));
        when(classRepository.findByIdAndSchoolId(1L, 1L)).thenReturn(Optional.of(schoolClass));
        when(academicYearRepository.findByIdAndSchoolId(1L, 1L)).thenReturn(Optional.of(academicYear));
        when(periodRepository.findById(1L)).thenReturn(Optional.of(period));
        
        when(timetableEntryRepository.findBySchoolIdAndSchoolClassIdAndSectionIdAndAcademicYearIdAndDayOfWeekAndPeriodId(
                1L, 1L, null, 1L, "MONDAY", 1L)).thenReturn(Optional.empty());

        TimetableEntry savedEntry = new TimetableEntry();
        savedEntry.setId(1L);
        savedEntry.setSchool(school);
        savedEntry.setSchoolClass(schoolClass);
        savedEntry.setAcademicYear(academicYear);
        savedEntry.setPeriod(period);
        savedEntry.setDayOfWeek("MONDAY");
        
        when(timetableEntryRepository.save(any())).thenReturn(savedEntry);

        List<TimetableEntryResponse> responses = academicService.saveTimetableGrid(request);
        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(1L, responses.get(0).id());
        verify(timetableEntryRepository, times(1)).save(any());
    }

    @Test
    void deleteTimetableEntry_ShouldDeleteSuccessfully() {
        TimetableEntry entry = new TimetableEntry();
        entry.setId(1L);
        entry.setSchool(school);

        when(authContextService.resolveSchoolId(1L)).thenReturn(1L);
        when(timetableEntryRepository.findById(1L)).thenReturn(Optional.of(entry));

        academicService.deleteTimetableEntry(1L, 1L);

        verify(timetableEntryRepository).delete(entry);
    }
}
