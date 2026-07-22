package com.school.erp.service.superadmin;

import com.school.erp.dto.superadmin.PlanDto;
import com.school.erp.entity.PlatformModule;
import com.school.erp.entity.SubscriptionPlan;
import com.school.erp.exception.ResourceNotFoundException;
import com.school.erp.repository.PlatformModuleRepository;
import com.school.erp.repository.SubscriptionPlanRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SuperAdminPlanService {

    private final SubscriptionPlanRepository planRepository;
    private final PlatformModuleRepository moduleRepository;

    public SuperAdminPlanService(SubscriptionPlanRepository planRepository, PlatformModuleRepository moduleRepository) {
        this.planRepository = planRepository;
        this.moduleRepository = moduleRepository;
    }

    @Transactional(readOnly = true)
    public List<PlanDto> getAllPlans() {
        return planRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PlanDto getPlan(Long id) {
        SubscriptionPlan plan = planRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription Plan not found for id " + id));
        return toDto(plan);
    }

    @Transactional
    public PlanDto createPlan(PlanDto dto) {
        SubscriptionPlan plan = new SubscriptionPlan();
        updateEntityFromDto(plan, dto);
        SubscriptionPlan saved = planRepository.save(plan);
        return toDto(saved);
    }

    @Transactional
    public PlanDto updatePlan(Long id, PlanDto dto) {
        SubscriptionPlan plan = planRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription Plan not found for id " + id));
        updateEntityFromDto(plan, dto);
        SubscriptionPlan saved = planRepository.save(plan);
        return toDto(saved);
    }

    @Transactional
    public PlanDto updateStatus(Long id, String status) {
        SubscriptionPlan plan = planRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription Plan not found for id " + id));
        plan.setStatus(status);
        SubscriptionPlan saved = planRepository.save(plan);
        return toDto(saved);
    }

    @Transactional
    public void deletePlan(Long id) {
        SubscriptionPlan plan = planRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription Plan not found for id " + id));
        planRepository.delete(plan);
    }

    private void updateEntityFromDto(SubscriptionPlan plan, PlanDto dto) {
        plan.setName(dto.getName());
        if (dto.getCode() != null) {
            plan.setCode(dto.getCode());
        } else if (plan.getCode() == null && dto.getName() != null) {
            plan.setCode(dto.getName().toUpperCase().replaceAll("\\s+", "_"));
        }
        plan.setMonthlyPrice(dto.getMonthlyPrice());
        plan.setAnnualPrice(dto.getAnnualPrice());
        plan.setDescription(dto.getDescription());
        if (dto.getStatus() != null) {
            plan.setStatus(dto.getStatus());
        } else if (plan.getStatus() == null) {
            plan.setStatus("ACTIVE");
        }
        if (dto.getLimits() != null) {
            plan.setMaxStudents(dto.getLimits().getMaxStudents());
            plan.setStorageGb(dto.getLimits().getStorageGb());
        }
        if (dto.getFeatures() != null) {
            String featuresString = "[" + dto.getFeatures().stream()
                    .map(f -> "\"" + f.replace("\"", "") + "\"")
                    .collect(Collectors.joining(",")) + "]";
            plan.setFeatures(featuresString);
        }
        if (dto.getModuleCodes() != null) {
            Set<PlatformModule> modules = new HashSet<>();
            for (String code : dto.getModuleCodes()) {
                moduleRepository.findByCode(code).ifPresent(modules::add);
            }
            plan.setModules(modules);
        }
    }

    private PlanDto toDto(SubscriptionPlan plan) {
        List<String> featureList = new ArrayList<>();
        if (plan.getFeatures() != null && !plan.getFeatures().isEmpty()) {
            String raw = plan.getFeatures().replace("[", "").replace("]", "").replace("\"", "");
            for (String part : raw.split(",")) {
                if (!part.trim().isEmpty()) {
                    featureList.add(part.trim());
                }
            }
        }
        List<String> moduleCodes = plan.getModules().stream()
                .map(PlatformModule::getCode)
                .collect(Collectors.toList());

        return PlanDto.builder()
                .id(plan.getId())
                .code(plan.getCode() != null ? plan.getCode() : (plan.getName() != null ? plan.getName().toUpperCase().replaceAll("\\s+", "_") : "PLAN"))
                .name(plan.getName())
                .monthlyPrice(plan.getMonthlyPrice())
                .annualPrice(plan.getAnnualPrice())
                .description(plan.getDescription())
                .status(plan.getStatus())
                .features(featureList)
                .moduleCodes(moduleCodes)
                .limits(new PlanDto.Limits(plan.getMaxStudents(), plan.getStorageGb()))
                .build();
    }
}
