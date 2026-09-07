package com.school.erp.service.hr;

import com.school.erp.entity.hr.SchoolDesignation;
import com.school.erp.repository.hr.SchoolDesignationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SchoolDesignationService {

    private final SchoolDesignationRepository repository;

    public List<SchoolDesignation> getAllDesignations(Long schoolId) {
        return repository.findBySchoolId(schoolId);
    }

    @Transactional
    public SchoolDesignation createDesignation(SchoolDesignation designation) {
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
