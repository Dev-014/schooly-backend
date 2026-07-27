package com.school.erp.service.superadmin;

import com.school.erp.dto.superadmin.ModuleDto;
import com.school.erp.entity.PlatformModule;
import com.school.erp.repository.PlatformModuleRepository;
import com.school.erp.repository.SchoolModuleAccessRepository;
import com.school.erp.repository.SchoolRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SuperAdminModuleServiceTest {

    private PlatformModuleRepository moduleRepo;
    private SchoolRepository schoolRepo;
    private SchoolModuleAccessRepository accessRepo;
    private SuperAdminModuleService service;

    @BeforeEach
    void setUp() {
        moduleRepo = mock(PlatformModuleRepository.class);
        schoolRepo = mock(SchoolRepository.class);
        accessRepo = mock(SchoolModuleAccessRepository.class);
        service = new SuperAdminModuleService(moduleRepo, schoolRepo, accessRepo);
    }

    @Test
    void getAllModules_ShouldReturnMappedDtos() {
        PlatformModule pm = new PlatformModule();
        pm.setId(1L);
        pm.setCode("FRONT_OFFICE");
        pm.setName("Front Desk");
        pm.setDescription("Desc");
        pm.setCategory("CORE");
        pm.setAddOnPrice(new BigDecimal("499.00"));
        pm.setTargetRoles("ADMIN");
        pm.setSubModules("[\"Admission Enquiry\"]");
        pm.setDefault(true);
        pm.setStatus("ACTIVE");

        when(moduleRepo.findAll()).thenReturn(Collections.singletonList(pm));

        List<ModuleDto> res = service.getAllModules();
        assertEquals(1, res.size());
        assertEquals("FRONT_OFFICE", res.get(0).getCode());
        assertEquals("CORE", res.get(0).getCategory());
        assertEquals(Arrays.asList("ADMIN"), res.get(0).getTargetRoles());
        assertEquals(Arrays.asList("Admission Enquiry"), res.get(0).getSubModules());
    }

    @Test
    void createModule_ShouldSaveAndReturnDto() {
        ModuleDto input = new ModuleDto(
                null, "TRANSPORT", "Transport", "Bus tracking", false, "ACTIVE",
                "OPERATION", new BigDecimal("1199.00"), Arrays.asList("ADMIN", "PARENT"), Arrays.asList("Routes")
        );

        when(moduleRepo.save(any(PlatformModule.class))).thenAnswer(inv -> {
            PlatformModule m = inv.getArgument(0);
            m.setId(10L);
            return m;
        });

        ModuleDto res = service.createModule(input);
        assertNotNull(res.getId());
        assertEquals("TRANSPORT", res.getCode());
        assertEquals("OPERATION", res.getCategory());
        verify(moduleRepo, times(1)).save(any(PlatformModule.class));
    }

    @Test
    void updateModule_ShouldUpdateFieldsAndSave() {
        PlatformModule pm = new PlatformModule();
        pm.setId(5L);
        pm.setCode("LMS_CLASSWORK");
        pm.setName("Old Name");

        when(moduleRepo.findById(5L)).thenReturn(Optional.of(pm));
        when(moduleRepo.save(any(PlatformModule.class))).thenAnswer(inv -> inv.getArgument(0));

        ModuleDto input = new ModuleDto(
                5L, "LMS_CLASSWORK", "New LMS Name", "Updated desc", true, "ACTIVE",
                "ACADEMIC", new BigDecimal("1499.00"), Arrays.asList("ADMIN", "TEACHER"), Arrays.asList("Lesson Planner")
        );

        ModuleDto res = service.updateModule(5L, input);
        assertEquals("New LMS Name", res.getName());
        assertEquals("ACADEMIC", res.getCategory());
        assertTrue(res.isDefault());
        verify(moduleRepo, times(1)).save(pm);
    }
}
