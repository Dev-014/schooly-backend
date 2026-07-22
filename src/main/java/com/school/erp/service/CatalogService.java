package com.school.erp.service;

import com.school.erp.dto.catalog.*;
import com.school.erp.entity.PlatformModule;
import com.school.erp.entity.SubscriptionPlan;
import com.school.erp.repository.PlatformModuleRepository;
import com.school.erp.repository.SubscriptionPlanRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CatalogService {

    private final SubscriptionPlanRepository planRepo;
    private final PlatformModuleRepository moduleRepo;

    public CatalogService(SubscriptionPlanRepository planRepo, PlatformModuleRepository moduleRepo) {
        this.planRepo = planRepo;
        this.moduleRepo = moduleRepo;
    }

    @Transactional(readOnly = true)
    public List<CatalogPlanDto> getActivePlans() {
        return planRepo.findAllByStatus("ACTIVE").stream().map(this::toPlanDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CatalogModuleDto> getActiveModules() {
        return moduleRepo.findAllByStatus("ACTIVE").stream().map(this::toModuleDto).collect(Collectors.toList());
    }

    public List<CatalogRoleDto> getSystemRoles() {
        return Arrays.asList(
            new CatalogRoleDto("SUPER_ADMIN", "Super Admin", "Full platform access and multi-tenant governance", true, Arrays.asList("ALL_PERMISSIONS")),
            new CatalogRoleDto("SCHOOL_ADMIN", "School Administrator", "Full administrative oversight of school operations and setup", true, Arrays.asList("SCHOOL_SETUP", "USER_MANAGEMENT", "FINANCE_OVERVIEW")),
            new CatalogRoleDto("PRINCIPAL", "Principal / HOD", "Academic leadership, staff performance, and exam approvals", true, Arrays.asList("ACADEMIC_OVERVIEW", "STAFF_MANAGEMENT", "EXAM_APPROVAL")),
            new CatalogRoleDto("TEACHER", "Faculty / Teacher", "Classroom grading, daily attendance, lesson planning, and homework", true, Arrays.asList("ATTENDANCE_MARK", "EXAM_GRADE", "LMS_ACCESS")),
            new CatalogRoleDto("ACCOUNTANT", "Finance Accountant", "Invoicing, fee collections, expense logging, and payroll", true, Arrays.asList("FINANCE_MANAGE", "INVOICE_CREATE", "PAYMENT_PROCESS")),
            new CatalogRoleDto("LIBRARIAN", "Head Librarian", "Book cataloging, member circulation, and fine processing", true, Arrays.asList("LIBRARY_MANAGE", "CIRCULATION")),
            new CatalogRoleDto("TRANSPORT_MANAGER", "Transport Manager", "Route mapping, bus tracking, and driver assignments", true, Arrays.asList("TRANSPORT_MANAGE", "BUS_TRACKING")),
            new CatalogRoleDto("STUDENT", "Student Portal", "Access grades, attendance history, classwork, and fee receipts", true, Arrays.asList("STUDENT_PORTAL")),
            new CatalogRoleDto("PARENT", "Parent / Guardian", "Monitor ward attendance, report cards, and online fee payments", true, Arrays.asList("PARENT_PORTAL", "FEE_PAY"))
        );
    }

    public List<CatalogOptionDto> getAcademicCycles() {
        return Arrays.asList(
            new CatalogOptionDto("2025-2026", "Academic Year 2025-2026", "Current active institutional cycle (June - May)", true),
            new CatalogOptionDto("2026-2027", "Academic Year 2026-2027", "Upcoming institutional cycle", false),
            new CatalogOptionDto("2024-2025", "Academic Year 2024-2025", "Past historical cycle for reference and reporting", false)
        );
    }

    public List<CatalogOptionDto> getGradeLevels() {
        return Arrays.asList(
            new CatalogOptionDto("KG", "Kindergarten (Pre-K / KG)", "Early childhood and foundational stage", true),
            new CatalogOptionDto("ELEM", "Elementary School (Grades 1-5)", "Primary education core stage", true),
            new CatalogOptionDto("MIDDLE", "Middle School (Grades 6-8)", "Preparatory middle stage", true),
            new CatalogOptionDto("HIGH", "High School (Grades 9-12)", "Secondary and senior secondary academic stage", true)
        );
    }

    private CatalogPlanDto toPlanDto(SubscriptionPlan plan) {
        List<String> featureList = new ArrayList<>();
        if (plan.getFeatures() != null && !plan.getFeatures().isEmpty()) {
            String raw = plan.getFeatures().replace("[", "").replace("]", "").replace("\"", "");
            for (String part : raw.split(",")) {
                if (!part.trim().isEmpty()) {
                    featureList.add(part.trim());
                }
            }
        }
        List<String> bundledCodes = plan.getModules().stream().map(PlatformModule::getCode).collect(Collectors.toList());
        return new CatalogPlanDto(
            plan.getId(),
            plan.getCode() != null ? plan.getCode() : plan.getName().toUpperCase().replaceAll("\\s+", "_"),
            plan.getName(),
            plan.getMonthlyPrice(),
            plan.getAnnualPrice(),
            plan.getMaxStudents(),
            plan.getStorageGb(),
            plan.getDescription(),
            featureList,
            bundledCodes,
            plan.getStatus()
        );
    }

    private CatalogModuleDto toModuleDto(PlatformModule module) {
        return new CatalogModuleDto(
            module.getId(),
            module.getCode(),
            module.getName(),
            module.getDescription(),
            module.getCategory() != null ? module.getCategory() : "CORE",
            module.getAddOnPrice(),
            module.isDefault(),
            module.getStatus()
        );
    }
}
