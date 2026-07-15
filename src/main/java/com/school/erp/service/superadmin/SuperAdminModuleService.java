package com.school.erp.service.superadmin;

import com.school.erp.dto.superadmin.ModuleDto;
import com.school.erp.entity.PlatformModule;
import com.school.erp.entity.School;
import com.school.erp.entity.SchoolModuleAccess;
import com.school.erp.repository.PlatformModuleRepository;
import com.school.erp.repository.SchoolModuleAccessRepository;
import com.school.erp.repository.SchoolRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SuperAdminModuleService {

    private final PlatformModuleRepository moduleRepo;
    private final SchoolRepository schoolRepo;
    private final SchoolModuleAccessRepository accessRepo;

    public SuperAdminModuleService(PlatformModuleRepository moduleRepo,
                                   SchoolRepository schoolRepo,
                                   SchoolModuleAccessRepository accessRepo) {
        this.moduleRepo = moduleRepo;
        this.schoolRepo = schoolRepo;
        this.accessRepo = accessRepo;
    }

    @Transactional(readOnly = true)
    public List<ModuleDto> getAllModules() {
        return moduleRepo.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ModuleDto createModule(ModuleDto dto) {
        PlatformModule module = new PlatformModule();
        module.setCode(dto.getCode());
        module.setName(dto.getName());
        module.setDescription(dto.getDescription());
        module.setDefault(dto.isDefault());
        module.setStatus(dto.getStatus());
        PlatformModule saved = moduleRepo.save(module);
        return toDto(saved);
    }

    @Transactional
    public void toggleModuleForSchool(Long schoolId, String moduleCode, boolean enabled) {
        School school = schoolRepo.findById(schoolId)
                .orElseThrow(() -> new com.school.erp.exception.ResourceNotFoundException("School not found for id " + schoolId));
        PlatformModule module = moduleRepo.findByCode(moduleCode)
                .orElseThrow(() -> new com.school.erp.exception.ResourceNotFoundException("Module not found for code " + moduleCode));
        SchoolModuleAccess access = accessRepo.findBySchoolAndModule(school, module)
                .orElseGet(() -> new SchoolModuleAccess(school, module));
        access.setEnabled(enabled);
        accessRepo.save(access);
    }

    private ModuleDto toDto(PlatformModule module) {
        return new ModuleDto(
                module.getId(),
                module.getCode(),
                module.getName(),
                module.getDescription(),
                module.isDefault(),
                module.getStatus()
        );
    }
}
