package com.school.erp.controller.accounting;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.school.erp.dto.accounting.IncomeHeadRequest;
import com.school.erp.dto.accounting.IncomeHeadResponse;
import com.school.erp.dto.accounting.IncomeResponse;
import com.school.erp.security.AuthFilterConfig;
import com.school.erp.security.JwtAuthenticationFilter;
import com.school.erp.service.accounting.IncomeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = IncomeController.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class),
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = AuthFilterConfig.class)
        }
)
class IncomeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

    @MockitoBean
    private IncomeService incomeService;

    @MockitoBean
    private com.school.erp.service.auth.AuthorizationService authorizationService;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        com.school.erp.security.AuthContextHolder.set(
                new com.school.erp.security.AuthenticatedUser(100L, 1L, com.school.erp.entity.auth.UserRole.ADMIN)
        );
        when(authorizationService.hasPermission(any(), any(), any())).thenReturn(true);
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        com.school.erp.security.AuthContextHolder.clear();
    }

    @Test
    @DisplayName("GET /api/v1/income/heads should return list of income heads")
    void getAllIncomeHeads_ShouldReturnList() throws Exception {
        IncomeHeadResponse head = new IncomeHeadResponse();
        head.setId(1L);
        head.setName("Donation");
        head.setDescription("General Donations");

        when(incomeService.getAllIncomeHeads()).thenReturn(List.of(head));

        mockMvc.perform(get("/api/v1/income/heads")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Donation"));
    }

    @Test
    @DisplayName("GET /api/v1/income should return list of income records")
    void getAllIncomes_ShouldReturnList() throws Exception {
        IncomeResponse income = new IncomeResponse();
        income.setId(10L);
        income.setAmount(new BigDecimal("1500.00"));
        income.setIncomeFrom("John Doe");
        income.setDate(LocalDate.of(2026, 10, 12));

        when(incomeService.getAllIncomes()).thenReturn(List.of(income));

        mockMvc.perform(get("/api/v1/income")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10L))
                .andExpect(jsonPath("$[0].incomeFrom").value("John Doe"))
                .andExpect(jsonPath("$[0].amount").value(1500.00));
    }

    @Test
    @DisplayName("POST /api/v1/income/heads should create and return income head")
    void createIncomeHead_ShouldReturnCreated() throws Exception {
        IncomeHeadRequest req = new IncomeHeadRequest();
        req.setName("Sponsorship");
        req.setDescription("Event sponsorships");

        IncomeHeadResponse resp = new IncomeHeadResponse();
        resp.setId(2L);
        resp.setName("Sponsorship");
        resp.setDescription("Event sponsorships");

        when(incomeService.createIncomeHead(any(IncomeHeadRequest.class))).thenReturn(resp);

        mockMvc.perform(post("/api/v1/income/heads")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2L))
                .andExpect(jsonPath("$.name").value("Sponsorship"));
    }
}
