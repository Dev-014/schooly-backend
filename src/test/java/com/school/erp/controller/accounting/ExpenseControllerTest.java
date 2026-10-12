package com.school.erp.controller.accounting;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.school.erp.dto.accounting.ExpenseHeadRequest;
import com.school.erp.dto.accounting.ExpenseHeadResponse;
import com.school.erp.dto.accounting.ExpenseResponse;
import com.school.erp.security.AuthFilterConfig;
import com.school.erp.security.JwtAuthenticationFilter;
import com.school.erp.service.accounting.ExpenseService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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
        controllers = ExpenseController.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class),
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = AuthFilterConfig.class)
        }
)
class ExpenseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

    @MockitoBean
    private ExpenseService expenseService;

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
    @DisplayName("GET /api/v1/expenses/heads should return list of expense heads")
    void getAllExpenseHeads_ShouldReturnList() throws Exception {
        ExpenseHeadResponse head = new ExpenseHeadResponse();
        head.setId(1L);
        head.setName("Office Supplies");
        head.setDescription("General stationery and office supplies");

        when(expenseService.getAllExpenseHeads()).thenReturn(List.of(head));

        mockMvc.perform(get("/api/v1/expenses/heads")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Office Supplies"));
    }

    @Test
    @DisplayName("GET /api/v1/expenses should return list of expenses")
    void getAllExpenses_ShouldReturnList() throws Exception {
        ExpenseResponse expense = new ExpenseResponse();
        expense.setId(20L);
        expense.setAmount(new BigDecimal("450.00"));
        expense.setVendorName("Stationery Hub");
        expense.setDate(LocalDate.of(2026, 10, 12));

        when(expenseService.getAllExpenses()).thenReturn(List.of(expense));

        mockMvc.perform(get("/api/v1/expenses")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(20L))
                .andExpect(jsonPath("$[0].vendorName").value("Stationery Hub"))
                .andExpect(jsonPath("$[0].amount").value(450.00));
    }

    @Test
    @DisplayName("POST /api/v1/expenses/heads should create and return expense head")
    void createExpenseHead_ShouldReturnCreated() throws Exception {
        ExpenseHeadRequest req = new ExpenseHeadRequest();
        req.setName("Utilities");
        req.setDescription("Electricity and internet");

        ExpenseHeadResponse resp = new ExpenseHeadResponse();
        resp.setId(2L);
        resp.setName("Utilities");
        resp.setDescription("Electricity and internet");

        when(expenseService.createExpenseHead(any(ExpenseHeadRequest.class))).thenReturn(resp);

        mockMvc.perform(post("/api/v1/expenses/heads")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2L))
                .andExpect(jsonPath("$.name").value("Utilities"));
    }
}
