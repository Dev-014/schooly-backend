package com.school.erp.service.finance;

import com.school.erp.dto.finance.CollectionPlanItemRequest;
import com.school.erp.dto.finance.CollectionPlanItemResponse;
import com.school.erp.dto.finance.CollectionPlanRequest;
import com.school.erp.dto.finance.CollectionPlanResponse;
import com.school.erp.entity.CollectionPlan;
import com.school.erp.entity.CollectionPlanItem;
import com.school.erp.entity.School;
import com.school.erp.repository.CollectionPlanItemRepository;
import com.school.erp.repository.CollectionPlanRepository;
import com.school.erp.repository.SchoolRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class CollectionPlanService {

    private final CollectionPlanRepository collectionPlanRepository;
    private final CollectionPlanItemRepository collectionPlanItemRepository;
    private final SchoolRepository schoolRepository;

    public CollectionPlanService(CollectionPlanRepository collectionPlanRepository, CollectionPlanItemRepository collectionPlanItemRepository, SchoolRepository schoolRepository) {
        this.collectionPlanRepository = collectionPlanRepository;
        this.collectionPlanItemRepository = collectionPlanItemRepository;
        this.schoolRepository = schoolRepository;
    }

    public CollectionPlanResponse createPlan(Long schoolId, CollectionPlanRequest request) {
        School school = schoolRepository.findById(schoolId)
                .orElseThrow(() -> new RuntimeException("School not found"));

        CollectionPlan plan = new CollectionPlan();
        plan.setSchool(school);
        plan.setName(request.getName());
        plan.setDescription(request.getDescription());
        plan.setIsActive(request.getIsActive());

        if (request.getItems() != null) {
            for (CollectionPlanItemRequest itemReq : request.getItems()) {
                CollectionPlanItem item = new CollectionPlanItem();
                item.setCollectionPlan(plan);
                item.setLabel(itemReq.getLabel());
                item.setDueDate(itemReq.getDueDate());
                item.setAmountType(itemReq.getAmountType());
                item.setAmountValue(itemReq.getAmountValue());
                item.setSequenceOrder(itemReq.getSequenceOrder());
                plan.getItems().add(item);
            }
        }

        plan = collectionPlanRepository.save(plan);
        return mapToResponse(plan);
    }

    @Transactional(readOnly = true)
    public List<CollectionPlanResponse> getPlans(Long schoolId) {
        return collectionPlanRepository.findBySchoolId(schoolId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public CollectionPlanResponse updatePlan(Long id, Long schoolId, CollectionPlanRequest request) {
        CollectionPlan plan = collectionPlanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Collection Plan not found"));

        if (!plan.getSchool().getId().equals(schoolId)) {
            throw new RuntimeException("Collection Plan does not belong to the school");
        }

        plan.setName(request.getName());
        plan.setDescription(request.getDescription());
        plan.setIsActive(request.getIsActive());

        plan.getItems().clear();
        collectionPlanRepository.flush(); // ensure orphans are cleared

        if (request.getItems() != null) {
            for (CollectionPlanItemRequest itemReq : request.getItems()) {
                CollectionPlanItem item = new CollectionPlanItem();
                item.setCollectionPlan(plan);
                item.setLabel(itemReq.getLabel());
                item.setDueDate(itemReq.getDueDate());
                item.setAmountType(itemReq.getAmountType());
                item.setAmountValue(itemReq.getAmountValue());
                item.setSequenceOrder(itemReq.getSequenceOrder());
                plan.getItems().add(item);
            }
        }

        plan = collectionPlanRepository.save(plan);
        return mapToResponse(plan);
    }

    public void deletePlan(Long id, Long schoolId) {
        CollectionPlan plan = collectionPlanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Collection Plan not found"));

        if (!plan.getSchool().getId().equals(schoolId)) {
            throw new RuntimeException("Collection Plan does not belong to the school");
        }

        collectionPlanRepository.delete(plan);
    }

    private CollectionPlanResponse mapToResponse(CollectionPlan plan) {
        CollectionPlanResponse res = new CollectionPlanResponse();
        res.setId(plan.getId());
        res.setSchoolId(plan.getSchool().getId());
        res.setName(plan.getName());
        res.setDescription(plan.getDescription());
        res.setIsActive(plan.getIsActive());
        res.setCreatedAt(plan.getCreatedAt());
        res.setUpdatedAt(plan.getUpdatedAt());

        if (plan.getItems() != null) {
            res.setItems(plan.getItems().stream().map(item -> {
                CollectionPlanItemResponse iRes = new CollectionPlanItemResponse();
                iRes.setId(item.getId());
                iRes.setLabel(item.getLabel());
                iRes.setDueDate(item.getDueDate());
                iRes.setAmountType(item.getAmountType());
                iRes.setAmountValue(item.getAmountValue());
                iRes.setSequenceOrder(item.getSequenceOrder());
                return iRes;
            }).collect(Collectors.toList()));
        }

        return res;
    }
}
