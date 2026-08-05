package com.school.erp.controller.superadmin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.school.erp.dto.EmployeeDocumentDTO;
import com.school.erp.dto.UploadDocumentRequest;
import com.school.erp.security.AuthFilterConfig;
import com.school.erp.security.JwtAuthenticationFilter;
import com.school.erp.service.superadmin.EmployeeDocumentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
    controllers = EmployeeDocumentController.class,
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {JwtAuthenticationFilter.class, AuthFilterConfig.class})
)
class EmployeeDocumentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EmployeeDocumentService documentService;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
    }

    @Test
    void testGetAllDocuments() throws Exception {
        EmployeeDocumentDTO dto = new EmployeeDocumentDTO();
        dto.setId(1L);
        dto.setFileName("resume.pdf");

        when(documentService.getAllDocuments()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/v1/super-admin/documents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].fileName").value("resume.pdf"));
    }

    @Test
    void testUploadDocument() throws Exception {
        UploadDocumentRequest request = new UploadDocumentRequest();
        request.setEmployeeId(1L);
        request.setDocumentType("RESUME");
        request.setFileName("resume.pdf");
        request.setFileUrl("https://s3.aws.com/resume.pdf");

        EmployeeDocumentDTO dto = new EmployeeDocumentDTO();
        dto.setId(2L);
        dto.setFileName("resume.pdf");
        dto.setDocumentType("RESUME");

        when(documentService.uploadDocument(any())).thenReturn(dto);

        mockMvc.perform(post("/api/v1/super-admin/documents/upload")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.fileName").value("resume.pdf"));
    }
}
