package com.school.erp.service.library;

import com.school.erp.dto.library.LibraryCirculationResponse;
import com.school.erp.entity.hr.Staff;
import com.school.erp.entity.student.Student;
import com.school.erp.repository.hr.StaffRepository;
import com.school.erp.repository.library.LibraryBookRepository;
import com.school.erp.repository.library.LibraryCirculationRepository;
import com.school.erp.repository.library.LibraryMemberRepository;
import com.school.erp.repository.student.StudentRepository;
import com.school.erp.repository.superadmin.SchoolRepository;
import com.school.erp.security.AuthContextService;
import com.school.erp.security.AuthenticatedUser;
import com.school.erp.service.auth.AuthorizationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SchoolLibraryServiceTest {

    @Mock
    private AuthContextService authContextService;
    @Mock
    private LibraryBookRepository bookRepository;
    @Mock
    private LibraryMemberRepository memberRepository;
    @Mock
    private LibraryCirculationRepository circulationRepository;
    @Mock
    private SchoolRepository schoolRepository;
    @Mock
    private StudentRepository studentRepository;
    @Mock
    private StaffRepository staffRepository;
    @Mock
    private ObjectProvider<AuthorizationService> authorizationServiceProvider;
    @Mock
    private AuthorizationService authorizationService;

    private SchoolLibraryService libraryService;

    @BeforeEach
    void setUp() {
        libraryService = new SchoolLibraryService(
                authContextService,
                bookRepository,
                memberRepository,
                circulationRepository,
                schoolRepository,
                studentRepository,
                staffRepository,
                authorizationServiceProvider
        );
    }

    @Test
    @DisplayName("Non-admin staff user without library.book_list.view is scoped to their staffId")
    void testFilterCirculations_scopesToStaffId_forNonAdminStaff() {
        Long schoolId = 1000L;
        Long userId = 70L;
        Long staffId = 21L;

        when(authContextService.resolveSchoolId(schoolId)).thenReturn(schoolId);
        when(authContextService.getCurrentUserOrNull()).thenReturn(
                new AuthenticatedUser(userId, schoolId, null)
        );
        when(authorizationServiceProvider.getIfAvailable()).thenReturn(authorizationService);
        when(authorizationService.hasPermission(schoolId, userId, "library.book_list.view")).thenReturn(false);
        when(authorizationService.hasPermission(schoolId, userId, "library.book.edit")).thenReturn(false);

        Staff staff = new Staff();
        staff.setId(staffId);
        when(staffRepository.findByUserId(userId)).thenReturn(Optional.of(staff));

        when(circulationRepository.filterCirculations(
                eq(schoolId), eq(null), eq(null), eq(null), eq(null), eq(staffId), eq(null), any(PageRequest.class)
        )).thenReturn(new PageImpl<>(List.of()));

        Page<LibraryCirculationResponse> result = libraryService.filterCirculations(
                schoolId, null, null, null, null, null, null, PageRequest.of(0, 10)
        );

        assertThat(result.getContent()).isEmpty();
        verify(circulationRepository).filterCirculations(
                eq(schoolId), eq(null), eq(null), eq(null), eq(null), eq(staffId), eq(null), any(PageRequest.class)
        );
    }

    @Test
    @DisplayName("Admin user queries all circulations when no borrower filter is provided")
    void testFilterCirculations_allowsUnfilteredQuery_forAdmin() {
        Long schoolId = 1000L;
        Long userId = 1L;

        when(authContextService.resolveSchoolId(schoolId)).thenReturn(schoolId);
        when(authContextService.getCurrentUserOrNull()).thenReturn(
                new AuthenticatedUser(userId, schoolId, com.school.erp.entity.auth.UserRole.ADMIN)
        );

        when(circulationRepository.filterCirculations(
                eq(schoolId), eq(null), eq(null), eq(null), eq(null), eq(null), eq(null), any(PageRequest.class)
        )).thenReturn(new PageImpl<>(List.of()));

        Page<LibraryCirculationResponse> result = libraryService.filterCirculations(
                schoolId, null, null, null, null, null, null, PageRequest.of(0, 10)
        );

        assertThat(result).isNotNull();
        verify(circulationRepository).filterCirculations(
                eq(schoolId), eq(null), eq(null), eq(null), eq(null), eq(null), eq(null), any(PageRequest.class)
        );
    }
}
