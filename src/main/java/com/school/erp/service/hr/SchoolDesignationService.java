package com.school.erp.service.hr;

import com.school.erp.entity.hr.SchoolDesignation;
import com.school.erp.repository.hr.SchoolDesignationRepository;
import com.school.erp.entity.superadmin.School;
import com.school.erp.repository.superadmin.SchoolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SchoolDesignationService {

    private final SchoolDesignationRepository repository;
    private final SchoolRepository schoolRepository;

    public List<SchoolDesignation> getAllDesignations(Long schoolId) {
        return repository.findBySchoolId(schoolId);
    }

    @Transactional
    public SchoolDesignation createDesignation(Long schoolId, SchoolDesignation designation) {
        School school = schoolRepository.findById(schoolId)
                .orElseThrow(() -> new IllegalArgumentException("School not found: " + schoolId));
        designation.setSchool(school);
        if (designation.getStatus() == null || designation.getStatus().isEmpty()) {
            designation.setStatus("ACTIVE");
        }
        return repository.save(designation);
    }

    @Transactional
    public SchoolDesignation updateDesignation(Long id, SchoolDesignation details) {
        SchoolDesignation designation = repository.findById(id).orElseThrow();
        designation.setName(details.getName());
        designation.setDescription(details.getDescription());
        designation.setStatus(details.getStatus());
        return repository.save(designation);
    }

    @Transactional
    public void deleteDesignation(Long id) {
        repository.deleteById(id);
    }
}
