package com.school.erp.service.hr;

import com.school.erp.entity.hr.SchoolDepartment;
import com.school.erp.repository.hr.SchoolDepartmentRepository;
import com.school.erp.entity.superadmin.School;
import com.school.erp.repository.superadmin.SchoolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SchoolDepartmentService {

    private final SchoolDepartmentRepository repository;
    private final SchoolRepository schoolRepository;

    public List<SchoolDepartment> getAllDepartments(Long schoolId) {
        return repository.findBySchoolId(schoolId);
    }

    @Transactional
    public SchoolDepartment createDepartment(Long schoolId, SchoolDepartment department) {
        School school = schoolRepository.findById(schoolId)
                .orElseThrow(() -> new IllegalArgumentException("School not found: " + schoolId));
        department.setSchool(school);
        if (department.getStatus() == null || department.getStatus().isEmpty()) {
            department.setStatus("ACTIVE");
        }
        return repository.save(department);
    }

    @Transactional
    public SchoolDepartment updateDepartment(Long id, SchoolDepartment details) {
        SchoolDepartment department = repository.findById(id).orElseThrow();
        department.setName(details.getName());
        department.setDescription(details.getDescription());
        department.setStatus(details.getStatus());
        return repository.save(department);
    }

    @Transactional
    public void deleteDepartment(Long id) {
        repository.deleteById(id);
    }
}
