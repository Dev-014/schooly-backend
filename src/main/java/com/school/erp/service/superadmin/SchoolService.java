package com.school.erp.service.superadmin;

import com.school.erp.dto.school.SchoolRequest;
import com.school.erp.dto.school.SchoolResponse;
import com.school.erp.entity.superadmin.School;
import com.school.erp.exception.ResourceNotFoundException;
import com.school.erp.repository.superadmin.SchoolRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class SchoolService {

    private final SchoolRepository schoolRepository;
    private final com.school.erp.service.auth.RoleManagementService roleManagementService;
    private final com.school.erp.repository.superadmin.PlatformModuleRepository platformModuleRepository;
    private final com.school.erp.repository.superadmin.SchoolModuleAccessRepository schoolModuleAccessRepository;

    public SchoolService(SchoolRepository schoolRepository, 
                         com.school.erp.service.auth.RoleManagementService roleManagementService,
                         com.school.erp.repository.superadmin.PlatformModuleRepository platformModuleRepository,
                         com.school.erp.repository.superadmin.SchoolModuleAccessRepository schoolModuleAccessRepository) {
        this.schoolRepository = schoolRepository;
        this.roleManagementService = roleManagementService;
        this.platformModuleRepository = platformModuleRepository;
        this.schoolModuleAccessRepository = schoolModuleAccessRepository;
    }

    @org.springframework.cache.annotation.Cacheable(value = com.school.erp.config.CacheConfig.CACHE_SCHOOL_STATUS, key = "#schoolId")
    public boolean isSchoolSuspended(Long schoolId) {
        if (schoolId == null) return false;
        return schoolRepository.findById(schoolId)
                .map(s -> "SUSPENDED".equalsIgnoreCase(s.getStatus()))
                .orElse(false);
    }

    public List<SchoolResponse> getAllSchools() {
        return schoolRepository.findAll().stream().map(this::toResponse).toList();
    }

    public SchoolResponse getSchoolById(Long id) {
        return toResponse(findSchool(id));
    }

    @Transactional
    public SchoolResponse createSchool(SchoolRequest request) {
        School school = new School();
        mapRequestToEntity(school, request);
        School saved = schoolRepository.save(school);
        
        // Grant default platform modules
        platformModuleRepository.findAllByStatus("ACTIVE").stream()
                .filter(com.school.erp.entity.superadmin.PlatformModule::isDefault)
                .forEach(module -> {
                    com.school.erp.entity.superadmin.SchoolModuleAccess access = new com.school.erp.entity.superadmin.SchoolModuleAccess();
                    access.setSchool(saved);
                    access.setModule(module);
                    access.setEnabled(true);
                    schoolModuleAccessRepository.save(access);
                });
                
        roleManagementService.seedDefaultRolesForSchool(saved.getId());
        return toResponse(saved);
    }

    @Transactional
    @org.springframework.cache.annotation.CacheEvict(value = com.school.erp.config.CacheConfig.CACHE_SCHOOL_STATUS, key = "#id")
    public SchoolResponse updateSchool(Long id, SchoolRequest request) {
        School school = findSchool(id);
        mapRequestToEntity(school, request);
        return toResponse(schoolRepository.save(school));
    }

    @Transactional
    @org.springframework.cache.annotation.CacheEvict(value = com.school.erp.config.CacheConfig.CACHE_SCHOOL_STATUS, key = "#id")
    public void deleteSchool(Long id) {
        School school = findSchool(id);
        schoolRepository.delete(school);
    }

    private School findSchool(Long id) {
        return schoolRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("School not found for id " + id));
    }

    private void mapRequestToEntity(School school, SchoolRequest request) {
        school.setName(request.name());
        school.setCode(request.code());
        school.setContactEmail(request.contactEmail());
        school.setContactPhone(request.contactPhone());
        school.setAddress(request.address());
        school.setStatus(request.status());
    }

    private SchoolResponse toResponse(School school) {
        return new SchoolResponse(
                school.getId(),
                school.getName(),
                school.getCode(),
                school.getContactEmail(),
                school.getContactPhone(),
                school.getAddress(),
                school.getStatus(),
                school.getCreatedAt()
        );
    }
}
