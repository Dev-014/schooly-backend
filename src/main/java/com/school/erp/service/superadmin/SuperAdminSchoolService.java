package com.school.erp.service.superadmin;

import com.school.erp.dto.superadmin.SchoolDto;
import com.school.erp.entity.School;
import com.school.erp.repository.SchoolRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SuperAdminSchoolService {

    private final SchoolRepository schoolRepository;

    public SuperAdminSchoolService(SchoolRepository schoolRepository) {
        this.schoolRepository = schoolRepository;
    }

    @Transactional(readOnly = true)
    public List<SchoolDto> getAllSchools() {
        return schoolRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SchoolDto getSchool(Long id) {
        return schoolRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new com.school.erp.exception.ResourceNotFoundException("School not found for id " + id));
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
                .orElseThrow(() -> new com.school.erp.exception.ResourceNotFoundException("School not found for id " + id));
        updateEntityFromDto(school, dto);
        School saved = schoolRepository.save(school);
        return toDto(saved);
    }

    @Transactional
    public SchoolDto updateStatus(Long id, String status) {
        School school = schoolRepository.findById(id)
                .orElseThrow(() -> new com.school.erp.exception.ResourceNotFoundException("School not found for id " + id));
        school.setStatus(status);
        School saved = schoolRepository.save(school);
        return toDto(saved);
    }

    @Transactional
    public void deleteSchool(Long id) {
        School school = schoolRepository.findById(id)
                .orElseThrow(() -> new com.school.erp.exception.ResourceNotFoundException("School not found for id " + id));
        schoolRepository.delete(school);
    }

    private void updateEntityFromDto(School school, SchoolDto dto) {
        school.setName(dto.getName());
        school.setCode(dto.getCode());
        school.setContactEmail(dto.getContactEmail());
        school.setContactPhone(dto.getContactPhone());
        school.setAddress(dto.getAddress());
        school.setStatus(dto.getStatus());
    }

    private SchoolDto toDto(School school) {
        return new SchoolDto(
                school.getId(),
                school.getName(),
                school.getCode(),
                school.getAddress(),
                school.getContactEmail(),
                school.getContactPhone(),
                school.getStatus()
        );
    }
}
