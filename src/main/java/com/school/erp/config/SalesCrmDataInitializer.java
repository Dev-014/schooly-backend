package com.school.erp.config;

import com.school.erp.entity.crm.*;
import com.school.erp.repository.crm.*;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
@Order(Ordered.LOWEST_PRECEDENCE + 1)
public class SalesCrmDataInitializer implements ApplicationRunner {

    private final CrmLeadRepository leadRepository;
    private final CrmFollowUpRepository followUpRepository;
    private final CrmDemoRepository demoRepository;
    private final CrmQuotationRepository quotationRepository;
    private final CrmActivityLogRepository activityLogRepository;

    public SalesCrmDataInitializer(CrmLeadRepository leadRepository,
                                   CrmFollowUpRepository followUpRepository,
                                   CrmDemoRepository demoRepository,
                                   CrmQuotationRepository quotationRepository,
                                   CrmActivityLogRepository activityLogRepository) {
        this.leadRepository = leadRepository;
        this.followUpRepository = followUpRepository;
        this.demoRepository = demoRepository;
        this.quotationRepository = quotationRepository;
        this.activityLogRepository = activityLogRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        if (leadRepository.count() == 0) {
            seedCrmData();
        }
    }

    private void seedCrmData() {
        CrmLead lead1 = createLead("Sunrise International", "Mr. John Doe", "Bangalore", "CBSE", "9876543210", CrmPipelineStage.NEW);
        CrmLead lead2 = createLead("Global Tech School", "Ms. Alice Smith", "Mumbai", "ICSE", "9876543211", CrmPipelineStage.CONTACTED);
        CrmLead lead3 = createLead("Evergreen Academy", "Dr. Bob Brown", "Delhi", "CBSE", "9876543212", CrmPipelineStage.DEMO_SCHEDULED);
        CrmLead lead4 = createLead("Royal Heritage", "Mrs. White", "Chennai", "State", "9876543213", CrmPipelineStage.QUOTATION_SENT);

        // Follow ups
        createFollowUp(lead2, CrmFollowUpAction.CALL, LocalDateTime.now().plusDays(1), "Call to discuss requirements", CrmFollowUpStatus.PENDING);
        createFollowUp(lead3, CrmFollowUpAction.EMAIL, LocalDateTime.now().minusDays(1), "Sent introductory email", CrmFollowUpStatus.COMPLETED);

        // Demos
        createDemo(lead3, LocalDateTime.now().plusDays(2), CrmDemoMode.ONLINE, CrmDemoStatus.SCHEDULED);
        
        // Quotation
        createQuotation(lead4, "QT-2023-001", "Enterprise Plan", new BigDecimal("50000"), CrmQuotationStatus.SENT);
        
        // Activity logs
        createLog(lead1, "Lead Created", "Lead came through website contact form");
        createLog(lead2, "Status Changed", "Moved from NEW to CONTACTED");
    }

    private CrmLead createLead(String schoolName, String principalName, String city, String board, String mobile, CrmPipelineStage stage) {
        CrmLead lead = new CrmLead();
        lead.setSchoolName(schoolName);
        lead.setPrincipalName(principalName);
        lead.setCity(city);
        lead.setBoard(board);
        lead.setMobile(mobile);
        lead.setPipelineStage(stage);
        lead.setLeadSource(CrmLeadSource.WEBSITE);
        lead.setApproxStudentStrength(500);
        return leadRepository.save(lead);
    }

    private void createFollowUp(CrmLead lead, CrmFollowUpAction action, LocalDateTime date, String remarks, CrmFollowUpStatus status) {
        CrmFollowUp followUp = new CrmFollowUp();
        followUp.setLead(lead);
        followUp.setActionType(action);
        followUp.setScheduledDate(date);
        followUp.setRemarks(remarks);
        followUp.setStatus(status);
        followUpRepository.save(followUp);
    }

    private void createDemo(CrmLead lead, LocalDateTime date, CrmDemoMode mode, CrmDemoStatus status) {
        CrmDemo demo = new CrmDemo();
        demo.setLead(lead);
        demo.setDemoDate(date);
        demo.setMode(mode);
        demo.setStatus(status);
        demoRepository.save(demo);
    }

    private void createQuotation(CrmLead lead, String number, String planName, BigDecimal amount, CrmQuotationStatus status) {
        CrmQuotation q = new CrmQuotation();
        q.setLead(lead);
        q.setQuotationNumber(number);
        q.setPlanName(planName);
        q.setAmount(amount);
        q.setDiscount(BigDecimal.ZERO);
        q.setGst(amount.multiply(new BigDecimal("0.18")));
        q.setTotal(q.getAmount().add(q.getGst()));
        q.setExpiryDate(LocalDateTime.now().plusDays(30));
        q.setStatus(status);
        quotationRepository.save(q);
    }

    private void createLog(CrmLead lead, String type, String desc) {
        CrmActivityLog log = new CrmActivityLog();
        log.setLead(lead);
        log.setActivityType(type);
        log.setDescription(desc);
        activityLogRepository.save(log);
    }
}
