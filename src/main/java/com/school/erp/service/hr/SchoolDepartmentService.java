package com.school.erp.service.hr;

import com.school.erp.entity.hr.SchoolDepartment;
import com.school.erp.repository.hr.SchoolDepartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SchoolDepartmentService {

    private final SchoolDepartmentRepository repository;

    public List<SchoolDepartment> getAllDepartments(Long schoolId) {
        return repository.findBySchoolId(schoolId);
    }

    @Transactional
    public SchoolDepartment createDepartment(SchoolDepartment department) {
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
