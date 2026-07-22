package com.school.erp.config;

import com.school.erp.entity.AuditLog;
import com.school.erp.entity.PlatformModule;
import com.school.erp.entity.School;
import com.school.erp.entity.SchoolModuleAccess;
import com.school.erp.repository.AuditLogRepository;
import com.school.erp.repository.PlatformModuleRepository;
import com.school.erp.repository.SchoolModuleAccessRepository;
import com.school.erp.repository.SchoolRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;

@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class SuperAdminSampleDataInitializer implements ApplicationRunner {

    private final SchoolRepository schoolRepository;
    private final PlatformModuleRepository moduleRepository;
    private final SchoolModuleAccessRepository accessRepository;
    private final AuditLogRepository auditLogRepository;

    public SuperAdminSampleDataInitializer(SchoolRepository schoolRepository,
                                           PlatformModuleRepository moduleRepository,
                                           SchoolModuleAccessRepository accessRepository,
                                           AuditLogRepository auditLogRepository) {
        this.schoolRepository = schoolRepository;
        this.moduleRepository = moduleRepository;
        this.accessRepository = accessRepository;
        this.auditLogRepository = auditLogRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        if (schoolRepository.count() == 0) {
            seedSampleSchoolsAndLogs();
        }
    }

    private void seedSampleSchoolsAndLogs() {
        School s1 = createSchool("Greenwood International Academy", "GREENWOOD", "admin@greenwood.edu", "+1-555-0101", "1200 Campus Drive, New York, NY", "Active", "greenwood", "Enterprise", "Step 8: Activated", "Healthy");
        School s2 = createSchool("St. Xavier's High School", "STXAVIER", "principal@stxaviers.org", "+1-555-0102", "450 Michigan Ave, Chicago, IL", "Active", "stxavier", "Professional", "Step 8: Activated", "Healthy");
        School s3 = createSchool("Delhi Public School Main Campus", "DPSMAIN", "info@dpsmain.edu", "+1-555-0103", "789 Silicon Blvd, Seattle, WA", "Active", "dpsmain", "Enterprise", "Step 8: Activated", "Healthy");
        School s4 = createSchool("Oakridge International School", "OAKRIDGE", "admissions@oakridge.edu", "+1-555-0104", "320 Austin Parkway, Austin, TX", "Trial", "oakridge", "Professional", "Step 5: Data Import", "Warning");
        School s5 = createSchool("Springfield Elementary Academy", "SPRINGFIELD", "contact@springfield.org", "+1-555-0105", "742 Evergreen Terrace, Springfield, OR", "Active", "springfield", "Essential Academy", "Step 8: Activated", "Healthy");

        List<PlatformModule> modules = moduleRepository.findAll();
        for (School s : List.of(s1, s2, s3, s4, s5)) {
            for (PlatformModule m : modules) {
                if (accessRepository.findBySchoolAndModule(s, m).isEmpty()) {
                    accessRepository.save(new SchoolModuleAccess(s, m));
                }
            }
        }

        if (auditLogRepository.count() == 0) {
            AuditLog l1 = new AuditLog();
            l1.setActorName("Dr. Alistair Finch (Super Admin)");
            l1.setAction("PLAN_UPGRADE_EXECUTE");
            l1.setResourceType("Subscription");
            l1.setTargetSchoolName("Greenwood International Academy");
            l1.setChangesJson("{\"old_plan\": \"Professional\", \"new_plan\": \"Enterprise\"}");
            l1.setIpAddress("192.168.1.104");
            l1.setStatus("SUCCESS");
            auditLogRepository.save(l1);

            AuditLog l2 = new AuditLog();
            l2.setActorName("System Sync Daemon");
            l2.setAction("DATA_IMPORT_COMPLETE");
            l2.setResourceType("Student Register");
            l2.setTargetSchoolName("St. Xavier's High School");
            l2.setChangesJson("{\"imported_records\": 450}");
            l2.setIpAddress("10.0.1.24");
            l2.setStatus("SUCCESS");
            auditLogRepository.save(l2);

            AuditLog l3 = new AuditLog();
            l3.setActorName("Elena Rostova (School Administrator)");
            l3.setAction("AUTH_LOGIN_ATTEMPT");
            l3.setResourceType("Authentication");
            l3.setTargetSchoolName("Delhi Public School Main Campus");
            l3.setChangesJson("{\"reason\": \"Invalid OTP verification token\"}");
            l3.setIpAddress("172.16.4.88");
            l3.setStatus("FAILED");
            auditLogRepository.save(l3);
        }
    }

    private School createSchool(String name, String code, String email, String phone, String address, String status, String subdomain, String plan, String step, String health) {
        School school = new School();
        school.setName(name);
        school.setCode(code);
        school.setContactEmail(email);
        school.setContactPhone(phone);
        school.setAddress(address);
        school.setStatus(status);
        school.setSubdomain(subdomain);
        school.setMetadata(new HashMap<>());
        school.getMetadata().put("plan", plan);
        school.getMetadata().put("onboardingStep", step);
        school.getMetadata().put("healthStatus", health);
        return schoolRepository.save(school);
    }
}
