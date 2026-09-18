package com.school.erp.service.impl;

import com.school.erp.dto.feecategory.FeeCategoryRequest;
import com.school.erp.dto.feecategory.FeeCategoryResponse;
import com.school.erp.dto.feestructure.FeeStructureItemRequest;
import com.school.erp.dto.feestructure.FeeStructureItemResponse;
import com.school.erp.dto.feestructure.FeeStructureRequest;
import com.school.erp.dto.feestructure.FeeStructureResponse;
import com.school.erp.entity.CollectionPlan;
import com.school.erp.entity.FeeCategory;
import com.school.erp.entity.FeeStructure;
import com.school.erp.entity.FeeStructureItem;
import com.school.erp.entity.School;
import com.school.erp.entity.SchoolClass;
import com.school.erp.repository.CollectionPlanRepository;
import com.school.erp.repository.FeeCategoryRepository;
import com.school.erp.repository.FeeStructureItemRepository;
import com.school.erp.repository.FeeStructureRepository;
import com.school.erp.repository.SchoolClassRepository;
import com.school.erp.repository.SchoolRepository;
import com.school.erp.service.FeeMasterService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class FeeMasterServiceImpl implements FeeMasterService {

    private final FeeCategoryRepository feeCategoryRepository;
    private final FeeStructureRepository feeStructureRepository;
    private final FeeStructureItemRepository feeStructureItemRepository;
    private final SchoolRepository schoolRepository;
    private final SchoolClassRepository schoolClassRepository;
    private final CollectionPlanRepository collectionPlanRepository;

    public FeeMasterServiceImpl(FeeCategoryRepository feeCategoryRepository,
                                FeeStructureRepository feeStructureRepository,
                                FeeStructureItemRepository feeStructureItemRepository,
                                SchoolRepository schoolRepository,
                                SchoolClassRepository schoolClassRepository,
                                CollectionPlanRepository collectionPlanRepository) {
        this.feeCategoryRepository = feeCategoryRepository;
        this.feeStructureRepository = feeStructureRepository;
        this.feeStructureItemRepository = feeStructureItemRepository;
        this.schoolRepository = schoolRepository;
        this.schoolClassRepository = schoolClassRepository;
        this.collectionPlanRepository = collectionPlanRepository;
    }

    private School getSchool(Long schoolId) {
        return schoolRepository.findById(schoolId).orElseThrow(() -> new RuntimeException("School not found"));
    }

    // --- Categories ---
    @Override
    public List<FeeCategoryResponse> getAllCategories(Long schoolId) {
        return feeCategoryRepository.findBySchoolId(schoolId).stream().map(this::mapToCategoryResponse).collect(Collectors.toList());
    }

    @Override
    public FeeCategoryResponse createCategory(FeeCategoryRequest request) {
        School school = getSchool(request.getSchoolId());
        FeeCategory category = new FeeCategory();
        category.setSchool(school);
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        return mapToCategoryResponse(feeCategoryRepository.save(category));
    }

    @Override
    public FeeCategoryResponse updateCategory(Long id, Long schoolId, FeeCategoryRequest request) {
        FeeCategory category = feeCategoryRepository.findById(id).orElseThrow(() -> new RuntimeException("Category not found"));
        if (!category.getSchool().getId().equals(schoolId)) throw new RuntimeException("Unauthorized access to category");
        
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        if (request.getIsActive() != null) {
            category.setIsActive(request.getIsActive());
        }
        return mapToCategoryResponse(feeCategoryRepository.save(category));
    }

    @Override
    public void deleteCategory(Long id, Long schoolId) {
        FeeCategory category = feeCategoryRepository.findById(id).orElseThrow(() -> new RuntimeException("Category not found"));
        if (!category.getSchool().getId().equals(schoolId)) throw new RuntimeException("Unauthorized access to category");
        feeCategoryRepository.delete(category);
    }

    // --- Structures ---
    @Override
    public List<FeeStructureResponse> getAllStructures(Long schoolId, Long academicYearId) {
        List<FeeStructure> structures;
        if (academicYearId != null) {
            structures = feeStructureRepository.findBySchoolIdAndAcademicYearId(schoolId, academicYearId);
        } else {
            structures = feeStructureRepository.findBySchoolId(schoolId);
        }
        return structures.stream().map(this::mapToStructureResponse).collect(Collectors.toList());
    }

    @Override
    public FeeStructureResponse getStructureById(Long id, Long schoolId) {
        FeeStructure structure = feeStructureRepository.findById(id).orElseThrow(() -> new RuntimeException("Structure not found"));
        if (!structure.getSchool().getId().equals(schoolId)) throw new RuntimeException("Unauthorized access to structure");
        return mapToStructureResponse(structure);
    }

    @Override
    public FeeStructureResponse createStructure(FeeStructureRequest request) {
        School school = getSchool(request.getSchoolId());
        SchoolClass schoolClass = schoolClassRepository.findById(request.getClassId()).orElseThrow(() -> new RuntimeException("Class not found"));
        
        FeeStructure structure = new FeeStructure();
        structure.setSchool(school);
        structure.setSchoolClass(schoolClass);
        structure.setAcademicYearId(request.getAcademicYearId());
        structure.setName(request.getName());
        structure.setDescription(request.getDescription());
        structure.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        if (request.getCollectionPlanId() != null) {
            structure.setCollectionPlan(collectionPlanRepository.findById(request.getCollectionPlanId()).orElse(null));
        }
        
        FeeStructure savedStructure = feeStructureRepository.save(structure);

        if (request.getItems() != null) {
            for (FeeStructureItemRequest itemReq : request.getItems()) {
                FeeCategory category = feeCategoryRepository.findById(itemReq.getFeeCategoryId())
                        .orElseThrow(() -> new RuntimeException("Category not found"));
                FeeStructureItem item = new FeeStructureItem();
                item.setFeeStructure(savedStructure);
                item.setFeeCategory(category);
                item.setAmount(itemReq.getAmount());
                item.setIsPartOfCollectionPlan(itemReq.getIsPartOfCollectionPlan() != null ? itemReq.getIsPartOfCollectionPlan() : true);
                feeStructureItemRepository.save(item);
                savedStructure.getItems().add(item);
            }
        }
        return mapToStructureResponse(savedStructure);
    }

    @Override
    public FeeStructureResponse updateStructure(Long id, Long schoolId, FeeStructureRequest request) {
        FeeStructure structure = feeStructureRepository.findById(id).orElseThrow(() -> new RuntimeException("Structure not found"));
        if (!structure.getSchool().getId().equals(schoolId)) throw new RuntimeException("Unauthorized access to structure");

        SchoolClass schoolClass = schoolClassRepository.findById(request.getClassId()).orElseThrow(() -> new RuntimeException("Class not found"));

        structure.setSchoolClass(schoolClass);
        structure.setAcademicYearId(request.getAcademicYearId());
        structure.setName(request.getName());
        structure.setDescription(request.getDescription());
        if (request.getIsActive() != null) {
            structure.setIsActive(request.getIsActive());
        }
        if (request.getCollectionPlanId() != null) {
            structure.setCollectionPlan(collectionPlanRepository.findById(request.getCollectionPlanId()).orElse(null));
        } else {
            structure.setCollectionPlan(null);
        }

        // Simplistic approach: delete existing items and recreate
        feeStructureItemRepository.deleteAll(structure.getItems());
        structure.getItems().clear();
        feeStructureRepository.saveAndFlush(structure);

        if (request.getItems() != null) {
            for (FeeStructureItemRequest itemReq : request.getItems()) {
                FeeCategory category = feeCategoryRepository.findById(itemReq.getFeeCategoryId())
                        .orElseThrow(() -> new RuntimeException("Category not found"));
                FeeStructureItem item = new FeeStructureItem();
                item.setFeeStructure(structure);
                item.setFeeCategory(category);
                item.setAmount(itemReq.getAmount());
                item.setIsPartOfCollectionPlan(itemReq.getIsPartOfCollectionPlan() != null ? itemReq.getIsPartOfCollectionPlan() : true);
                feeStructureItemRepository.save(item);
                structure.getItems().add(item);
            }
        }

        return mapToStructureResponse(structure);
    }

    @Override
    public void deleteStructure(Long id, Long schoolId) {
        FeeStructure structure = feeStructureRepository.findById(id).orElseThrow(() -> new RuntimeException("Structure not found"));
        if (!structure.getSchool().getId().equals(schoolId)) throw new RuntimeException("Unauthorized access to structure");
        feeStructureRepository.delete(structure);
    }

    // --- Mappers ---
    private FeeCategoryResponse mapToCategoryResponse(FeeCategory entity) {
        FeeCategoryResponse res = new FeeCategoryResponse();
        res.setId(entity.getId());
        res.setSchoolId(entity.getSchool().getId());
        res.setName(entity.getName());
        res.setDescription(entity.getDescription());
        res.setIsActive(entity.getIsActive());
        res.setCreatedAt(entity.getCreatedAt());
        return res;
    }

    private FeeStructureResponse mapToStructureResponse(FeeStructure entity) {
        FeeStructureResponse res = new FeeStructureResponse();
        res.setId(entity.getId());
        res.setSchoolId(entity.getSchool().getId());
        if (entity.getSchoolClass() != null) {
            res.setClassId(entity.getSchoolClass().getId());
            res.setClassName(entity.getSchoolClass().getName());
        }
        res.setAcademicYearId(entity.getAcademicYearId());
        res.setName(entity.getName());
        res.setDescription(entity.getDescription());
        res.setIsActive(entity.getIsActive());
        if (entity.getCollectionPlan() != null) {
            res.setCollectionPlanId(entity.getCollectionPlan().getId());
            res.setCollectionPlanName(entity.getCollectionPlan().getName());
        }
        res.setCreatedAt(entity.getCreatedAt());
        if (entity.getItems() != null) {
            res.setItems(entity.getItems().stream().map(item -> {
                FeeStructureItemResponse itemRes = new FeeStructureItemResponse();
                itemRes.setId(item.getId());
                itemRes.setFeeCategoryId(item.getFeeCategory().getId());
                itemRes.setFeeCategoryName(item.getFeeCategory().getName());
                itemRes.setAmount(item.getAmount());
                itemRes.setIsPartOfCollectionPlan(item.getIsPartOfCollectionPlan());
                return itemRes;
            }).collect(Collectors.toList()));
        }
        return res;
    }
}
