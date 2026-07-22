package com.school.erp.service.superadmin;

import com.school.erp.dto.superadmin.SchoolDto;
import com.school.erp.entity.School;
import com.school.erp.entity.SchoolModuleAccess;
import com.school.erp.exception.ResourceNotFoundException;
import com.school.erp.repository.SchoolModuleAccessRepository;
import com.school.erp.repository.SchoolRepository;
import com.school.erp.repository.StaffRepository;
import com.school.erp.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SuperAdminSchoolService {

    private final SchoolRepository schoolRepository;
    private final StudentRepository studentRepository;
    private final StaffRepository staffRepository;
    private final SchoolModuleAccessRepository accessRepository;

    public SuperAdminSchoolService(SchoolRepository schoolRepository,
                                   StudentRepository studentRepository,
                                   StaffRepository staffRepository,
                                   SchoolModuleAccessRepository accessRepository) {
        this.schoolRepository = schoolRepository;
        this.studentRepository = studentRepository;
        this.staffRepository = staffRepository;
        this.accessRepository = accessRepository;
    }

    @Transactional(readOnly = true)
    public List<SchoolDto> getAllSchools() {
        return getAllSchools(null, null, null);
    }

    @Transactional(readOnly = true)
    public List<SchoolDto> getAllSchools(String status, String plan, String search) {
        return schoolRepository.findAll().stream()
                .filter(school -> status == null || status.equalsIgnoreCase("All") || status.isEmpty() || status.equalsIgnoreCase(school.getStatus()))
                .filter(school -> {
                    if (plan == null || plan.equalsIgnoreCase("All") || plan.isEmpty()) return true;
                    String schoolPlan = getSchoolPlan(school);
                    return schoolPlan.equalsIgnoreCase(plan);
                })
                .filter(school -> {
                    if (search == null || search.trim().isEmpty()) return true;
                    String q = search.trim().toLowerCase();
                    return (school.getName() != null && school.getName().toLowerCase().contains(q)) ||
                           (school.getCode() != null && school.getCode().toLowerCase().contains(q)) ||
                           (school.getSubdomain() != null && school.getSubdomain().toLowerCase().contains(q));
                })
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SchoolDto getSchool(Long id) {
        return schoolRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("School not found for id " + id));
    }

    @Transactional
    public SchoolDto createSchool(SchoolDto dto) {
        School school = new School();
        updateEntityFromDto(school, dto);
        School saved = schoolRepository.save(school);
        return toDto(saved);
    }

    @Transactional
    public SchoolDto updateSchool(Long id, SchoolDto dto) {
        School school = schoolRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("School not found for id " + id));
        updateEntityFromDto(school, dto);
        School saved = schoolRepository.save(school);
        return toDto(saved);
    }

    @Transactional
    public SchoolDto updateStatus(Long id, String status) {
        School school = schoolRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("School not found for id " + id));
        school.setStatus(status);
        School saved = schoolRepository.save(school);
        return toDto(saved);
    }

    @Transactional
    public void deleteSchool(Long id) {
        School school = schoolRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("School not found for id " + id));
        schoolRepository.delete(school);
    }

    private void updateEntityFromDto(School school, SchoolDto dto) {
        if (dto.getName() != null) school.setName(dto.getName());
        if (dto.getCode() != null) school.setCode(dto.getCode());
        if (dto.getContactEmail() != null) school.setContactEmail(dto.getContactEmail());
        if (dto.getContactPhone() != null) school.setContactPhone(dto.getContactPhone());
        if (dto.getAddress() != null) school.setAddress(dto.getAddress());
        if (dto.getStatus() != null) {
            school.setStatus(dto.getStatus());
        } else if (school.getStatus() == null) {
            school.setStatus("Active");
        }
        if (dto.getSubdomain() != null) {
            school.setSubdomain(dto.getSubdomain());
        } else if (school.getSubdomain() == null && school.getCode() != null) {
            school.setSubdomain(school.getCode().toLowerCase());
        }
        if (school.getMetadata() == null) {
            school.setMetadata(new HashMap<>());
        }
        if (dto.getMetadata() != null) {
            school.getMetadata().putAll(dto.getMetadata());
        }
        if (dto.getPlan() != null) {
            school.getMetadata().put("plan", dto.getPlan());
        } else if (school.getMetadata().get("plan") == null) {
            school.getMetadata().put("plan", "Professional");
        }
        if (dto.getHealthStatus() != null) {
            school.getMetadata().put("healthStatus", dto.getHealthStatus());
        }
        if (dto.getOnboardingStep() != null) {
            school.getMetadata().put("onboardingStep", dto.getOnboardingStep());
        }
    }

    private String getSchoolPlan(School school) {
        if (school.getMetadata() != null && school.getMetadata().get("plan") != null) {
            return school.getMetadata().get("plan").toString();
        }
        return "Professional";
    }

    private SchoolDto toDto(School school) {
        int studentCount = (int) studentRepository.countBySchoolId(school.getId());
        int staffCount = (int) staffRepository.countBySchoolId(school.getId());
        int modulesCount = (int) accessRepository.findBySchoolId(school.getId()).stream()
                .filter(a -> Boolean.TRUE.equals(a.getEnabled()))
                .count();

        String subdomain = school.getSubdomain() != null ? school.getSubdomain() : (school.getCode() != null ? school.getCode().toLowerCase() : "campus");
        String domain = subdomain + ".schooly.io";
        String plan = getSchoolPlan(school);

        String healthStatus = "Healthy";
        if ("Suspended".equalsIgnoreCase(school.getStatus()) || "Inactive".equalsIgnoreCase(school.getStatus())) {
            healthStatus = "Critical";
        } else if ("Trial".equalsIgnoreCase(school.getStatus()) || "Draft".equalsIgnoreCase(school.getStatus())) {
            healthStatus = "Warning";
        } else if (school.getMetadata() != null && school.getMetadata().get("healthStatus") != null) {
            healthStatus = school.getMetadata().get("healthStatus").toString();
        }

        String onboardingStep = "Step 8: Activated";
        if (school.getMetadata() != null && school.getMetadata().get("onboardingStep") != null) {
            onboardingStep = school.getMetadata().get("onboardingStep").toString();
        }

        String createdAtStr = school.getCreatedAt() != null ? school.getCreatedAt().toString() : "2026-07-01T10:00:00";

        return SchoolDto.builder()
                .id(school.getId())
                .name(school.getName())
                .code(school.getCode())
                .address(school.getAddress())
                .contactEmail(school.getContactEmail())
                .contactPhone(school.getContactPhone())
                .status(school.getStatus() != null ? school.getStatus() : "Active")
                .subdomain(subdomain)
                .domain(domain)
                .plan(plan)
                .studentCount(studentCount)
                .staffCount(staffCount)
                .modulesCount(modulesCount)
                .healthStatus(healthStatus)
                .onboardingStep(onboardingStep)
                .createdAt(createdAtStr)
                .metadata(school.getMetadata())
                .build();
    }
}
