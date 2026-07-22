package com.school.erp.service.superadmin;

import com.school.erp.dto.superadmin.PlanDto;
import com.school.erp.entity.SubscriptionPlan;
import com.school.erp.repository.PlatformModuleRepository;
import com.school.erp.repository.SubscriptionPlanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SuperAdminPlanServiceTest {

    @Mock
    private SubscriptionPlanRepository planRepository;

    @Mock
    private PlatformModuleRepository moduleRepository;

    private SuperAdminPlanService planService;

    @BeforeEach
    void setUp() {
        planService = new SuperAdminPlanService(planRepository, moduleRepository);
    }

    @Test
    void shouldReturnAllPlans() {
        SubscriptionPlan plan = new SubscriptionPlan();
        plan.setId(1L);
        plan.setName("Starter Plan");
        plan.setCode("STARTER");
        plan.setMonthlyPrice(new BigDecimal("99.00"));
        plan.setStatus("ACTIVE");

        when(planRepository.findAll()).thenReturn(Collections.singletonList(plan));

        List<PlanDto> result = planService.getAllPlans();
        assertEquals(1, result.size());
        assertEquals("STARTER", result.get(0).getCode());
    }

    @Test
    void shouldCreatePlan() {
        PlanDto dto = PlanDto.builder()
                .name("Pro Plan")
                .code("PRO")
                .monthlyPrice(new BigDecimal("299.00"))
                .status("ACTIVE")
                .build();

        SubscriptionPlan saved = new SubscriptionPlan();
        saved.setId(10L);
        saved.setName("Pro Plan");
        saved.setCode("PRO");
        saved.setMonthlyPrice(new BigDecimal("299.00"));
        saved.setStatus("ACTIVE");

        when(planRepository.save(any(SubscriptionPlan.class))).thenReturn(saved);

        PlanDto result = planService.createPlan(dto);
        assertNotNull(result);
        assertEquals("PRO", result.getCode());
    }
}
