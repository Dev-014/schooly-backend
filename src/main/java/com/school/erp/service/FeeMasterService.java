package com.school.erp.service;

import com.school.erp.dto.feecategory.FeeCategoryRequest;
import com.school.erp.dto.feecategory.FeeCategoryResponse;
import com.school.erp.dto.feestructure.FeeStructureRequest;
import com.school.erp.dto.feestructure.FeeStructureResponse;

import java.util.List;

public interface FeeMasterService {
    // Categories
    List<FeeCategoryResponse> getAllCategories(Long schoolId);
    FeeCategoryResponse createCategory(FeeCategoryRequest request);
    FeeCategoryResponse updateCategory(Long id, Long schoolId, FeeCategoryRequest request);
    void deleteCategory(Long id, Long schoolId);

    // Structures
    List<FeeStructureResponse> getAllStructures(Long schoolId, Long academicYearId);
    FeeStructureResponse getStructureById(Long id, Long schoolId);
    FeeStructureResponse createStructure(FeeStructureRequest request);
    FeeStructureResponse updateStructure(Long id, Long schoolId, FeeStructureRequest request);
    void deleteStructure(Long id, Long schoolId);
}
