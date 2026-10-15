package com.school.erp.controller.admin.library;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.school.erp.dto.library.LibraryBookRequest;
import com.school.erp.dto.library.LibraryBookResponse;
import com.school.erp.dto.library.LibraryCirculationResponse;
import com.school.erp.dto.library.LibraryMemberRequest;
import com.school.erp.dto.library.LibraryMemberResponse;
import com.school.erp.dto.library.LibraryStatsResponse;
import com.school.erp.security.AuthFilterConfig;
import com.school.erp.security.JwtAuthenticationFilter;
import com.school.erp.service.library.SchoolLibraryService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = AdminLibraryController.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class),
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = AuthFilterConfig.class)
        }
)
class AdminLibraryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

    @MockitoBean
    private SchoolLibraryService libraryService;

    @MockitoBean
    private com.school.erp.service.auth.AuthorizationService authorizationService;

    @BeforeEach
    void setUp() {
        com.school.erp.security.AuthContextHolder.set(
                new com.school.erp.security.AuthenticatedUser(100L, 1L, com.school.erp.entity.auth.UserRole.ADMIN)
        );
        when(authorizationService.hasPermission(any(), any(), any())).thenReturn(true);
    }

    @AfterEach
    void tearDown() {
        com.school.erp.security.AuthContextHolder.clear();
    }

    @Test
    @DisplayName("GET /api/v1/admin/library/books should return page of books")
    void getBooks_ShouldReturnPage() throws Exception {
        LibraryBookResponse book = LibraryBookResponse.builder()
                .id(1L)
                .title("Clean Code")
                .author("Robert C. Martin")
                .status("IN STOCK")
                .build();

        when(libraryService.filterBooks(any(), any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(book)));

        mockMvc.perform(get("/api/v1/admin/library/books")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data[0].id").value(1L))
                .andExpect(jsonPath("$.data[0].title").value("Clean Code"));
    }

    @Test
    @DisplayName("GET /api/v1/admin/library/stats should return library statistics")
    void getStats_ShouldReturnStats() throws Exception {
        LibraryStatsResponse stats = LibraryStatsResponse.builder()
                .totalBooks(1500L)
                .availableCopies(1200L)
                .issuedBooks(300L)
                .overdueBooks(10L)
                .totalMembers(250L)
                .build();

        when(libraryService.getStats(any())).thenReturn(stats);

        mockMvc.perform(get("/api/v1/admin/library/stats")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.totalBooks").value(1500L))
                .andExpect(jsonPath("$.data.availableCopies").value(1200L));
    }

    @Test
    @DisplayName("POST /api/v1/admin/library/books should create book")
    void createBook_ShouldReturnCreated() throws Exception {
        LibraryBookRequest request = LibraryBookRequest.builder()
                .title("The Pragmatic Programmer")
                .author("Andy Hunt & Dave Thomas")
                .build();

        LibraryBookResponse response = LibraryBookResponse.builder()
                .id(2L)
                .title("The Pragmatic Programmer")
                .author("Andy Hunt & Dave Thomas")
                .build();

        when(libraryService.createBook(any(), any(LibraryBookRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/admin/library/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.id").value(2L))
                .andExpect(jsonPath("$.data.title").value("The Pragmatic Programmer"));
    }

    @Test
    @DisplayName("GET /api/v1/admin/library/members should return page of members")
    void getMembers_ShouldReturnPage() throws Exception {
        LibraryMemberResponse member = LibraryMemberResponse.builder()
                .id(10L)
                .memberType("STUDENT")
                .cardNumber("LIB-STU-001")
                .fullName("Elena Rodriguez")
                .status("Active")
                .maxBooksAllowed(3)
                .build();

        when(libraryService.filterMembers(any(), any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(member)));

        mockMvc.perform(get("/api/v1/admin/library/members")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data[0].id").value(10L))
                .andExpect(jsonPath("$.data[0].fullName").value("Elena Rodriguez"))
                .andExpect(jsonPath("$.data[0].cardNumber").value("LIB-STU-001"));
    }

    @Test
    @DisplayName("POST /api/v1/admin/library/members should register new library member")
    void createMember_ShouldReturnCreated() throws Exception {
        LibraryMemberRequest request = LibraryMemberRequest.builder()
                .memberType("STUDENT")
                .cardNumber("LIB-STU-1001-A9B2")
                .fullName("Elena Rodriguez")
                .admissionNumber("AC-2023-458")
                .maxBooksAllowed(3)
                .status("Active")
                .studentId(1L)
                .build();

        LibraryMemberResponse response = LibraryMemberResponse.builder()
                .id(10L)
                .memberType("STUDENT")
                .cardNumber("LIB-STU-1001-A9B2")
                .fullName("Elena Rodriguez")
                .admissionNumber("AC-2023-458")
                .maxBooksAllowed(3)
                .status("Active")
                .studentId(1L)
                .build();

        when(libraryService.createMember(any(), any(LibraryMemberRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/admin/library/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.id").value(10L))
                .andExpect(jsonPath("$.data.cardNumber").value("LIB-STU-1001-A9B2"))
                .andExpect(jsonPath("$.data.fullName").value("Elena Rodriguez"));
    }

    @Test
    @DisplayName("DELETE /api/v1/admin/library/members/{id} should delete member")
    void deleteMember_ShouldReturnSuccess() throws Exception {
        mockMvc.perform(delete("/api/v1/admin/library/members/10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"));
    }

    @Test
    @DisplayName("GET /api/v1/admin/library/circulations should return page filtered by studentId")
    void getCirculations_WithStudentId_ShouldReturnPage() throws Exception {
        LibraryCirculationResponse circulation = LibraryCirculationResponse.builder()
                .id(100L)
                .bookId(1L)
                .bookTitle("To Kill a Mockingbird")
                .author("Harper Lee")
                .memberId(10L)
                .studentId(1L)
                .memberName("Elena Rodriguez")
                .memberCardNumber("LIB-STU-001")
                .status("Issued")
                .fineAmount(java.math.BigDecimal.ZERO)
                .finePaid(false)
                .build();

        when(libraryService.filterCirculations(any(), any(), any(), any(), eq(1L), any(), any()))
                .thenReturn(new PageImpl<>(List.of(circulation)));

        mockMvc.perform(get("/api/v1/admin/library/circulations")
                        .param("studentId", "1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data[0].id").value(100L))
                .andExpect(jsonPath("$.data[0].bookTitle").value("To Kill a Mockingbird"))
                .andExpect(jsonPath("$.data[0].studentId").value(1L));
    }
}
