package com.school.erp.controller.file;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.school.erp.dto.file.*;
import com.school.erp.enums.file.FileCategory;
import com.school.erp.enums.file.FileEntityType;
import com.school.erp.enums.file.FileStatus;
import com.school.erp.security.AuthContextHolder;
import com.school.erp.security.AuthFilterConfig;
import com.school.erp.security.AuthenticatedUser;
import com.school.erp.security.JwtAuthenticationFilter;
import com.school.erp.service.auth.AuthorizationService;
import com.school.erp.service.file.FileManagementService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = FileController.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class),
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = AuthFilterConfig.class)
        }
)
class FileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FileManagementService fileManagementService;

    @MockitoBean
    private AuthorizationService authorizationService;

    @MockitoBean
    private com.school.erp.security.AuthContextService authContextService;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        AuthContextHolder.set(new AuthenticatedUser(100L, 1L, com.school.erp.entity.auth.UserRole.ADMIN));
        when(authorizationService.hasPermission(anyLong(), anyLong(), anyString())).thenReturn(true);
    }

    @AfterEach
    void tearDown() {
        AuthContextHolder.clear();
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/v1/files/upload-init - returns presigned upload URL and metadata")
    void testUploadInit_Success() throws Exception {
        FileUploadInitRequest request = new FileUploadInitRequest(
                "admission_birth_cert.pdf",
                "application/pdf",
                1024L * 500,
                FileEntityType.STUDENT,
                10L,
                FileCategory.BIRTH_CERTIFICATE
        );

        UUID fileId = UUID.randomUUID();
        FileUploadInitResponse response = new FileUploadInitResponse(
                fileId,
                "https://schooly-dev-files.s3.ap-south-1.amazonaws.com/test-key?signature=xyz",
                "PUT",
                "schools/1/STUDENT/10/BIRTH_CERTIFICATE/" + fileId + ".pdf",
                Map.of("Content-Type", "application/pdf"),
                Instant.now().plusSeconds(900)
        );

        when(fileManagementService.initiateUpload(any(FileUploadInitRequest.class), anyString())).thenReturn(response);

        mockMvc.perform(post("/api/v1/files/upload-init")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.fileId").value(fileId.toString()))
                .andExpect(jsonPath("$.data.httpMethod").value("PUT"))
                .andExpect(jsonPath("$.data.uploadUrl").exists());
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/v1/files/{fileId}/complete - activates file and returns file details")
    void testCompleteUpload_Success() throws Exception {
        UUID fileId = UUID.randomUUID();
        FileResponseDto response = new FileResponseDto(
                fileId,
                1L,
                "birth_cert.pdf",
                "application/pdf",
                "pdf",
                1024L,
                FileStatus.ACTIVE,
                "schools/1/STUDENT/10/BIRTH_CERTIFICATE/" + fileId + ".pdf",
                FileCategory.BIRTH_CERTIFICATE,
                FileEntityType.STUDENT,
                10L,
                0,
                100L,
                LocalDateTime.now()
        );

        when(fileManagementService.completeUpload(eq(fileId), any(), anyString())).thenReturn(response);

        mockMvc.perform(post("/api/v1/files/" + fileId + "/complete")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"))
                .andExpect(jsonPath("$.data.id").value(fileId.toString()));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/v1/files/{fileId}/download - returns presigned download URL")
    void testDownload_Success() throws Exception {
        UUID fileId = UUID.randomUUID();
        FileDownloadResponse response = new FileDownloadResponse(
                fileId,
                "https://schooly-dev-files.s3.ap-south-1.amazonaws.com/test-key?get-sig=abc",
                "birth_cert.pdf",
                "application/pdf",
                1024L,
                true,
                Instant.now().plusSeconds(900)
        );

        when(fileManagementService.getDownloadUrl(eq(fileId), eq(true), anyString())).thenReturn(response);

        mockMvc.perform(get("/api/v1/files/" + fileId + "/download?preview=true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.downloadUrl").exists())
                .andExpect(jsonPath("$.data.isInline").value(true));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/v1/files - returns filtered files with pagination")
    void testListFiles_Success() throws Exception {
        UUID fileId = UUID.randomUUID();
        FileResponseDto fileDto = new FileResponseDto(
                fileId,
                1L,
                "report_card.pdf",
                "application/pdf",
                "pdf",
                2048L,
                FileStatus.ACTIVE,
                "schools/1/STUDENT/10/REPORT_CARD/" + fileId + ".pdf",
                FileCategory.REPORT_CARD,
                FileEntityType.STUDENT,
                10L,
                0,
                100L,
                LocalDateTime.now()
        );

        when(fileManagementService.listFiles(any())).thenReturn(
                new PageImpl<>(List.of(fileDto), PageRequest.of(0, 20), 1)
        );

        mockMvc.perform(get("/api/v1/files?entityType=STUDENT&entityId=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(fileId.toString()))
                .andExpect(jsonPath("$.pagination.total").value(1));
    }

    @Test
    @WithMockUser
    @DisplayName("DELETE /api/v1/files/{fileId} - deletes file")
    void testDelete_Success() throws Exception {
        UUID fileId = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/files/" + fileId).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"));
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/v1/files/initialize-folders - provisions standard S3 use-case folders")
    void testInitializeSchoolFolders_Success() throws Exception {
        when(authContextService.requireCurrentUser()).thenReturn(new AuthenticatedUser(100L, 1L, com.school.erp.entity.auth.UserRole.ADMIN));

        mockMvc.perform(post("/api/v1/files/initialize-folders?schoolId=1").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("Folders created"));
    }
}

