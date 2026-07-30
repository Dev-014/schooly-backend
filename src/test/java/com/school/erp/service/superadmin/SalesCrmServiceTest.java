package com.school.erp.service.superadmin;

import com.school.erp.dto.crm.CrmDashboardStatsDto;
import com.school.erp.entity.crm.CrmLead;
import com.school.erp.entity.crm.CrmPipelineStage;
import com.school.erp.repository.crm.*;
import com.school.erp.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SalesCrmServiceTest {

    @Mock
    private CrmLeadRepository leadRepository;

    @Mock
    private CrmFollowUpRepository followUpRepository;

    @Mock
    private CrmDemoRepository demoRepository;

    @Mock
    private CrmQuotationRepository quotationRepository;

    @Mock
    private CrmActivityLogRepository activityLogRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private SalesCrmService salesCrmService;

    @BeforeEach
    void setUp() {
    }

    @Test
    void testGetDashboardStats() {
        CrmLead lead1 = new CrmLead();
        lead1.setPipelineStage(CrmPipelineStage.NEW);

        CrmLead lead2 = new CrmLead();
        lead2.setPipelineStage(CrmPipelineStage.WON);
        
        when(leadRepository.count()).thenReturn(2L);
        when(leadRepository.findAll()).thenReturn(Arrays.asList(lead1, lead2));

        CrmDashboardStatsDto stats = salesCrmService.getDashboardStats();

        assertEquals(2L, stats.getTotalLeads());
        assertEquals(1L, stats.getNewLeads());
        assertEquals(1L, stats.getWonDeals());
        assertEquals(50.0, stats.getConversionRate());
    }
}
