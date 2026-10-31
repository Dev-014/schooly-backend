package com.school.erp.service.frontoffice;

import com.school.erp.dto.frontoffice.VisitorLogRequest;
import com.school.erp.dto.frontoffice.VisitorLogResponse;
import com.school.erp.entity.frontoffice.VisitorLog;
import com.school.erp.entity.superadmin.School;
import com.school.erp.repository.frontoffice.VisitorLogRepository;
import com.school.erp.repository.hr.StaffRepository;
import com.school.erp.repository.superadmin.SchoolRepository;
import com.school.erp.security.AuthContextService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VisitorLogServiceTest {

    @Mock
    private AuthContextService authContextService;

    @Mock
    private VisitorLogRepository visitorLogRepository;

    @Mock
    private SchoolRepository schoolRepository;

    @Mock
    private StaffRepository staffRepository;

    @InjectMocks
    private VisitorLogService visitorLogService;

    private School school;
    private VisitorLog visitor;

    @BeforeEach
    void setUp() {
        school = new School();
        school.setId(1L);
        school.setName("Springfield High");

        visitor = new VisitorLog();
        visitor.setId(10L);
        visitor.setSchool(school);
        visitor.setVisitorName("John Doe");
        visitor.setPurpose("Admission Inquiry");
        visitor.setMeetingWith("Principal");
        visitor.setPhone("9876543210");
        visitor.setEmail("john@example.com");
        visitor.setNumberOfPeople(2);
        visitor.setVisitDate(LocalDate.now());
        visitor.setTimeIn(LocalTime.of(10, 30));
        visitor.setStatus("ON_SITE");
    }

    @Test
    void testUpdateVisitor_Success() {
        when(authContextService.resolveSchoolId(1L)).thenReturn(1L);
        when(visitorLogRepository.findByIdAndSchoolId(10L, 1L)).thenReturn(Optional.of(visitor));
        when(visitorLogRepository.save(any(VisitorLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        VisitorLogRequest request = VisitorLogRequest.builder()
                .visitorName("Johnathan Doe")
                .purpose("Fee Payment")
                .meetingWith("Accountant")
                .phone("9998887776")
                .email("johnathan@example.com")
                .numberOfPeople(3)
                .idCardNumber("ID-999")
                .visitDate(LocalDate.of(2026, 11, 1))
                .timeIn(LocalTime.of(11, 0))
                .estTimeOut(LocalTime.of(12, 0))
                .note("Updated notes")
                .build();

        VisitorLogResponse response = visitorLogService.updateVisitor(1L, 10L, request);

        assertNotNull(response);
        assertEquals(10L, response.getId());
        assertEquals("Johnathan Doe", response.getVisitorName());
        assertEquals("Fee Payment", response.getPurpose());
        assertEquals("Accountant", response.getMeetingWith());
        assertEquals("9998887776", response.getPhone());
        assertEquals("johnathan@example.com", response.getEmail());
        assertEquals(3, response.getNumberOfPeople());
        assertEquals("ID-999", response.getIdCardNumber());
        assertEquals("Updated notes", response.getNote());

        verify(visitorLogRepository).save(visitor);
    }
}
