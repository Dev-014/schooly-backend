package com.school.erp.service.frontoffice;

import com.school.erp.dto.frontoffice.ComplaintRequest;
import com.school.erp.dto.frontoffice.ComplaintResponse;
import com.school.erp.entity.auth.UserRole;
import com.school.erp.entity.frontoffice.SchoolComplaint;
import com.school.erp.entity.superadmin.School;
import com.school.erp.repository.frontoffice.SchoolComplaintRepository;
import com.school.erp.repository.superadmin.SchoolRepository;
import com.school.erp.security.AuthContextService;
import com.school.erp.security.AuthenticatedUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SchoolComplaintServiceTest {

    @Mock
    private AuthContextService authContextService;

    @Mock
    private SchoolComplaintRepository schoolComplaintRepository;

    @Mock
    private SchoolRepository schoolRepository;

    @InjectMocks
    private SchoolComplaintService schoolComplaintService;

    private School school;
    private SchoolComplaint complaint;

    @BeforeEach
    void setUp() {
        school = new School();
        school.setId(1L);
        school.setName("Springfield High");

        complaint = new SchoolComplaint();
        complaint.setId(101L);
        complaint.setComplaintCode("CMP-00101");
        complaint.setSchool(school);
        complaint.setComplainantName("Alice Teacher");
        complaint.setComplaintType("Facilities & Maintenance");
        complaint.setDescription("Smart board projector bulb dead in Room 102");
        complaint.setStatus("Open");
        complaint.setCreatedByUserId(5L);
        complaint.setCreatedByRole("TEACHER");
        complaint.setSourceScope("OWN");
        complaint.setPriority("HIGH");
        complaint.setComplaintDate(LocalDate.now());
    }

    @Test
    void testCreateComplaint_generatesComplaintCodeAndAssignsUser() {
        when(authContextService.resolveSchoolId(1L)).thenReturn(1L);
        when(authContextService.getCurrentUserOrNull()).thenReturn(new AuthenticatedUser(5L, 1L, UserRole.TEACHER));
        when(schoolRepository.findById(1L)).thenReturn(Optional.of(school));

        SchoolComplaint initialSaved = new SchoolComplaint();
        initialSaved.setId(102L);
        initialSaved.setSchool(school);
        initialSaved.setComplainantName("Alice Teacher");
        initialSaved.setComplaintType("Facilities & Maintenance");
        initialSaved.setStatus("Open");

        when(schoolComplaintRepository.save(any(SchoolComplaint.class))).thenReturn(initialSaved);

        ComplaintRequest request = ComplaintRequest.builder()
                .complainantName("Alice Teacher")
                .complaintType("Facilities & Maintenance")
                .description("Smart board projector bulb dead in Room 102")
                .priority("HIGH")
                .build();

        ComplaintResponse response = schoolComplaintService.createComplaint(1L, request);

        assertNotNull(response);
        verify(schoolComplaintRepository, atLeastOnce()).save(any(SchoolComplaint.class));
    }

    @Test
    void testUpdateComplaint_whenOwnerOrAdmin_allowsFullEdit() {
        when(authContextService.resolveSchoolId(1L)).thenReturn(1L);
        when(authContextService.getCurrentUserOrNull()).thenReturn(new AuthenticatedUser(5L, 1L, UserRole.TEACHER));
        when(schoolComplaintRepository.findByIdAndSchoolId(101L, 1L)).thenReturn(Optional.of(complaint));
        when(schoolComplaintRepository.save(any(SchoolComplaint.class))).thenAnswer(i -> i.getArgument(0));

        ComplaintRequest updateReq = ComplaintRequest.builder()
                .complainantName("Alice Teacher Updated")
                .complaintType("Academic Inquiry")
                .description("Updated description")
                .status("In Progress")
                .actionTaken("Inspection scheduled")
                .build();

        ComplaintResponse updated = schoolComplaintService.updateComplaint(1L, 101L, updateReq);

        assertEquals("Alice Teacher Updated", updated.getComplainantName());
        assertEquals("Academic Inquiry", updated.getComplaintType());
        assertEquals("In Progress", updated.getStatus());
        assertEquals("Inspection scheduled", updated.getActionTaken());
    }

    @Test
    void testUpdateComplaint_whenNonOwnerNonAdmin_updatesStatusOnly() {
        when(authContextService.resolveSchoolId(1L)).thenReturn(1L);
        // Current user is User 99 (Receptionist/Staff, not owner of complaint 101 which was filed by User 5)
        when(authContextService.getCurrentUserOrNull()).thenReturn(new AuthenticatedUser(99L, 1L, UserRole.STAFF));
        when(schoolComplaintRepository.findByIdAndSchoolId(101L, 1L)).thenReturn(Optional.of(complaint));
        when(schoolComplaintRepository.save(any(SchoolComplaint.class))).thenAnswer(i -> i.getArgument(0));

        ComplaintRequest unauthorizedEditReq = ComplaintRequest.builder()
                .complainantName("Hacked Name")
                .complaintType("Hacked Type")
                .description("Hacked Description")
                .status("Resolved")
                .actionTaken("Resolved by administrative office")
                .build();

        ComplaintResponse response = schoolComplaintService.updateComplaint(1L, 101L, unauthorizedEditReq);

        // Name, type, description must NOT be overwritten!
        assertEquals("Alice Teacher", response.getComplainantName());
        assertEquals("Facilities & Maintenance", response.getComplaintType());
        assertEquals("Smart board projector bulb dead in Room 102", response.getDescription());
        // Status and actionTaken ARE updated!
        assertEquals("Resolved", response.getStatus());
        assertEquals("Resolved by administrative office", response.getActionTaken());
    }

    @Test
    void testFilterComplaints_withOwnScope_filtersByCurrentUserId() {
        when(authContextService.resolveSchoolId(1L)).thenReturn(1L);
        when(authContextService.getCurrentUserOrNull()).thenReturn(new AuthenticatedUser(5L, 1L, UserRole.TEACHER));

        Page<SchoolComplaint> mockPage = new PageImpl<>(List.of(complaint));
        when(schoolComplaintRepository.filterComplaints(eq(1L), isNull(), isNull(), isNull(), eq(5L), any(PageRequest.class)))
                .thenReturn(mockPage);

        Page<ComplaintResponse> result = schoolComplaintService.filterComplaints(
                1L, null, null, null, "own", PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
        assertEquals("CMP-00101", result.getContent().get(0).getComplaintCode());
        assertTrue(result.getContent().get(0).getIsOwn());
    }
}

