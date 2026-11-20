package com.school.erp.service.superadmin;

import com.school.erp.dto.superadmin.SchoolDto;
import com.school.erp.entity.superadmin.PlatformModule;
import com.school.erp.entity.superadmin.School;
import com.school.erp.entity.superadmin.SchoolModuleAccess;
import com.school.erp.entity.superadmin.SchoolSubscription;
import com.school.erp.entity.superadmin.SubscriptionPlan;
import com.school.erp.repository.hr.StaffRepository;
import com.school.erp.repository.student.StudentRepository;
import com.school.erp.repository.superadmin.SchoolModuleAccessRepository;
import com.school.erp.repository.superadmin.SchoolRepository;
import com.school.erp.repository.superadmin.SchoolSubscriptionRepository;
import com.school.erp.repository.superadmin.SubscriptionPlanRepository;
import com.school.erp.service.auth.RoleManagementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SuperAdminSchoolServiceTest {

    @Mock private SchoolRepository schoolRepository;
    @Mock private StudentRepository studentRepository;
    @Mock private StaffRepository staffRepository;
    @Mock private SchoolModuleAccessRepository accessRepository;
    @Mock private SubscriptionPlanRepository planRepository;
    @Mock private SchoolSubscriptionRepository subscriptionRepository;
    @Mock private RoleManagementService roleManagementService;

    private SuperAdminSchoolService service;

    @BeforeEach
    void setUp() {
        service = new SuperAdminSchoolService(
                schoolRepository,
                studentRepository,
                staffRepository,
                accessRepository,
                planRepository,
                subscriptionRepository,
                roleManagementService
        );
    }

    @Test
    void getAllSchools_mapsActiveSubscriptionFinancialsCanonicalPlanAndFiltersCleanly() {
        School school = new School();
        school.setId(10L);
        school.setName("Horizon Public School");
        school.setCode("HPS");
        school.setSubdomain("horizon");
        school.setCity("Pune");
        school.setState("Maharashtra");
        school.setContactEmail("office@horizon.edu.in");
        school.setContactPhone("9876543210");
        school.setStatus("ACTIVE");
        school.setOnboardingStatus("Step 6: Activated");
        school.setMetadata(Map.of(
                "boardType", "CBSE",
                "portalLoginPhone", "9999888877",
                "subscriptionPlan", "ENTERPRISE"
        ));

        SubscriptionPlan premiumPlan = new SubscriptionPlan();
        premiumPlan.setId(3L);
        premiumPlan.setCode("ENTERPRISE");
        premiumPlan.setName("Premium Enterprise Plan");

        SchoolSubscription sub = new SchoolSubscription();
        sub.setId(55L);
        sub.setSchool(school);
        sub.setPlan(premiumPlan);
        sub.setBillingPeriod("YEARLY");
        sub.setTotalStudents(500);
        sub.setTotalAmount(new BigDecimal("180000.00"));
        sub.setAmountPaid(new BigDecimal("90000.00"));
        sub.setRemainingAmount(new BigDecimal("90000.00"));
        sub.setStatus("ACTIVE");
        sub.setStartDate(LocalDate.of(2026, 11, 20));
        sub.setEndDate(LocalDate.of(2027, 11, 20));

        PlatformModule studentsMod = new PlatformModule();
        studentsMod.setCode("STUDENTS");
        SchoolModuleAccess enabledMvp = new SchoolModuleAccess(school, studentsMod);
        enabledMvp.setEnabled(true);

        PlatformModule inventoryMod = new PlatformModule();
        inventoryMod.setCode("INVENTORY");
        SchoolModuleAccess legacyPostMvp = new SchoolModuleAccess(school, inventoryMod);
        legacyPostMvp.setEnabled(true);

        when(schoolRepository.findAll()).thenReturn(List.of(school));
        when(studentRepository.countBySchoolId(10L)).thenReturn(420L);
        when(staffRepository.countBySchoolId(10L)).thenReturn(38L);
        when(accessRepository.findBySchoolId(10L)).thenReturn(List.of(enabledMvp, legacyPostMvp));
        when(subscriptionRepository.findFirstBySchoolIdAndStatusOrderByIdDesc(10L, "ACTIVE"))
                .thenReturn(Optional.of(sub));

        List<SchoolDto> results = service.getAllSchools("Active", "ENTERPRISE", "Pune");
        assertEquals(1, results.size());

        SchoolDto dto = results.get(0);
        assertEquals("Premium Plan", dto.getPlan());
        assertEquals("ENTERPRISE", dto.getPlanCode());
        assertEquals("Active", dto.getStatus());
        assertEquals("CBSE", dto.getBoardType());
        assertEquals("Pune", dto.getCity());
        assertEquals("9999888877", dto.getPortalLoginPhone());
        assertEquals(420, dto.getStudentCount());
        assertEquals(500, dto.getBilledStudentCount());
        assertEquals(38, dto.getStaffCount());
        assertEquals(1, dto.getModulesCount()); // excludes post-MVP INVENTORY
        assertEquals(new BigDecimal("180000.00"), dto.getTotalAmount());
        assertEquals(new BigDecimal("90000.00"), dto.getAmountPaid());
        assertEquals(new BigDecimal("90000.00"), dto.getRemainingAmount());
        assertEquals("PARTIAL", dto.getPaymentStatus());
        assertEquals("2027-11-20", dto.getRenewalDate());

        // Filtering by a different plan excludes the school
        assertTrue(service.getAllSchools("Active", "BASIC", null).isEmpty());
    }
}
