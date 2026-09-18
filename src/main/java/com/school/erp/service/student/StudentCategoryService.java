package com.school.erp.service;

import com.school.erp.dto.student.StudentCategoryRequest;
import com.school.erp.dto.student.StudentCategoryResponse;
import com.school.erp.entity.School;
import com.school.erp.entity.StudentCategory;
import com.school.erp.exception.ResourceNotFoundException;
import com.school.erp.repository.SchoolRepository;
import com.school.erp.repository.StudentCategoryRepository;
import com.school.erp.security.AuthContextService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class StudentCategoryService {

    private final StudentCategoryRepository categoryRepository;
    private final SchoolRepository schoolRepository;
    private final AuthContextService authContextService;

    public StudentCategoryService(StudentCategoryRepository categoryRepository, SchoolRepository schoolRepository, AuthContextService authContextService) {
        this.categoryRepository = categoryRepository;
        this.schoolRepository = schoolRepository;
        this.authContextService = authContextService;
    }

    public List<StudentCategoryResponse> getAllCategories(Long schoolId) {
        Long effectiveSchoolId = authContextService.resolveSchoolId(schoolId);
        return categoryRepository.findBySchoolId(effectiveSchoolId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public StudentCategoryResponse createCategory(StudentCategoryRequest request) {
        Long effectiveSchoolId = authContextService.resolveSchoolId(request.schoolId());
        School school = schoolRepository.findById(effectiveSchoolId)
                .orElseThrow(() -> new ResourceNotFoundException("School not found"));

        StudentCategory category = new StudentCategory();
        category.setSchool(school);
        category.setName(request.name());
        category.setDescription(request.description());

        return toResponse(categoryRepository.save(category));
    }

    @Transactional
    public StudentCategoryResponse updateCategory(Long id, StudentCategoryRequest request) {
        Long effectiveSchoolId = authContextService.resolveSchoolId(request.schoolId());
        StudentCategory category = categoryRepository.findByIdAndSchoolId(id, effectiveSchoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        category.setName(request.name());
        category.setDescription(request.description());

        return toResponse(categoryRepository.save(category));
    }

    @Transactional
    public void deleteCategory(Long id, Long schoolId) {
        Long effectiveSchoolId = authContextService.resolveSchoolId(schoolId);
        StudentCategory category = categoryRepository.findByIdAndSchoolId(id, effectiveSchoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        categoryRepository.delete(category);
    }

    private StudentCategoryResponse toResponse(StudentCategory category) {
        return new StudentCategoryResponse(
                category.getId(),
                category.getSchool().getId(),
                category.getName(),
                category.getDescription()
        );
    }
}
