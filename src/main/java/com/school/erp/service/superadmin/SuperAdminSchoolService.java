package com.school.erp.service.superadmin;

import com.school.erp.dto.superadmin.SchoolDto;
import com.school.erp.entity.superadmin.School;
import com.school.erp.entity.superadmin.SchoolSubscription;
import com.school.erp.entity.superadmin.SubscriptionPlan;
import com.school.erp.exception.ResourceNotFoundException;
import com.school.erp.repository.hr.StaffRepository;
import com.school.erp.repository.student.StudentRepository;
import com.school.erp.repository.superadmin.SchoolModuleAccessRepository;
import com.school.erp.repository.superadmin.SchoolRepository;
import com.school.erp.repository.superadmin.SchoolSubscriptionRepository;
import com.school.erp.repository.superadmin.SubscriptionPlanRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SuperAdminSchoolService {

    private static final Set<String> POST_MVP_DISABLED_MODULES = Set.of(
            "INVENTORY",
            "DISCIPLINE",
            "LESSON_MODULE",
            "CERTIFICATES",
            "DOWNLOADS",
            "ONLINE_CLASS",
            "ONLINE_EXAMS",
            "QUESTION_PAPER"
    );

    private final SchoolRepository schoolRepository;
    private final StudentRepository studentRepository;
    private final StaffRepository staffRepository;
    private final SchoolModuleAccessRepository accessRepository;
    private final SubscriptionPlanRepository planRepository;
    private final SchoolSubscriptionRepository subscriptionRepository;
    private final com.school.erp.service.auth.RoleManagementService roleManagementService;

    public SuperAdminSchoolService(SchoolRepository schoolRepository,
                                   StudentRepository studentRepository,
                                   StaffRepository staffRepository,
                                   SchoolModuleAccessRepository accessRepository,
                                   SubscriptionPlanRepository planRepository,
                                   SchoolSubscriptionRepository subscriptionRepository,
                                   com.school.erp.service.auth.RoleManagementService roleManagementService) {
        this.schoolRepository = schoolRepository;
        this.studentRepository = studentRepository;
        this.staffRepository = staffRepository;
        this.accessRepository = accessRepository;
        this.planRepository = planRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.roleManagementService = roleManagementService;
    }

    @Transactional(readOnly = true)
    public List<SchoolDto> getAllSchools() {
        return getAllSchools(null, null, null);
    }

    @Transactional(readOnly = true)
    public List<SchoolDto> getAllSchools(String status, String plan, String search) {
        return schoolRepository.findAll().stream()
                .map(this::toDto)
                .filter(dto -> status == null || status.equalsIgnoreCase("All") || status.isBlank()
                        || status.equalsIgnoreCase(dto.getStatus()))
                .filter(dto -> {
                    if (plan == null || plan.equalsIgnoreCase("All") || plan.isBlank()) return true;
                    String canonicalFilter = canonicalPlanCode(plan);
                    if (canonicalFilter != null && canonicalFilter.equalsIgnoreCase(dto.getPlanCode())) {
                        return true;
                    }
                    return dto.getPlan() != null && dto.getPlan().toLowerCase().contains(plan.trim().toLowerCase());
                })
                .filter(dto -> {
                    if (search == null || search.trim().isEmpty()) return true;
                    String q = search.trim().toLowerCase();
                    return containsIgnoreCase(dto.getName(), q)
                            || containsIgnoreCase(dto.getCode(), q)
                            || containsIgnoreCase(dto.getSubdomain(), q)
                            || containsIgnoreCase(dto.getDomain(), q)
                            || containsIgnoreCase(dto.getCity(), q)
                            || containsIgnoreCase(dto.getContactEmail(), q)
                            || containsIgnoreCase(dto.getContactPhone(), q);
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SchoolDto getSchool(Long id) {
        return schoolRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("School not found for id " + id));
    }

    @Transactional
    public SchoolDto createSchool(SchoolDto dto) {
        School school = new School();
        updateEntityFromDto(school, dto);
        School saved = schoolRepository.save(school);
        roleManagementService.seedDefaultRolesForSchool(saved.getId());
        return toDto(saved);
    }

    @Transactional
    public SchoolDto updateSchool(Long id, SchoolDto dto) {
        School school = schoolRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("School not found for id " + id));
        updateEntityFromDto(school, dto);
        School saved = schoolRepository.save(school);
        return toDto(saved);
    }

    @Transactional
    public SchoolDto updateStatus(Long id, String status) {
        School school = schoolRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("School not found for id " + id));
        school.setStatus(normalizeStatusForStorage(status));
        School saved = schoolRepository.save(school);
        return toDto(saved);
    }

    @Transactional
    public void deleteSchool(Long id) {
        School school = schoolRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("School not found for id " + id));
        schoolRepository.delete(school);
    }

    private void updateEntityFromDto(School school, SchoolDto dto) {
        if (dto.getName() != null) school.setName(dto.getName());
        if (dto.getCode() != null) school.setCode(dto.getCode());
        if (dto.getContactEmail() != null) school.setContactEmail(dto.getContactEmail());
        if (dto.getContactPhone() != null) school.setContactPhone(dto.getContactPhone());
        if (dto.getAddress() != null) school.setAddress(dto.getAddress());
        if (dto.getCity() != null) school.setCity(dto.getCity());
        if (dto.getState() != null) school.setState(dto.getState());
        if (dto.getLogoUrl() != null) school.setLogoUrl(dto.getLogoUrl());
        if (dto.getStatus() != null) {
            school.setStatus(normalizeStatusForStorage(dto.getStatus()));
        } else if (school.getStatus() == null) {
            school.setStatus("ACTIVE");
        }
        if (dto.getSubdomain() != null) {
            school.setSubdomain(dto.getSubdomain());
        } else if (school.getSubdomain() == null && school.getCode() != null) {
            school.setSubdomain(school.getCode().toLowerCase());
        }
        if (school.getMetadata() == null) {
            school.setMetadata(new HashMap<>());
        }
        if (dto.getMetadata() != null) {
            school.getMetadata().putAll(dto.getMetadata());
        }
        if (dto.getPlan() != null) {
            SubscriptionPlan subPlan = planRepository.findAll().stream()
                    .filter(p -> p.getName().equalsIgnoreCase(dto.getPlan())
                            || (p.getCode() != null && p.getCode().equalsIgnoreCase(dto.getPlan())))
                    .findFirst()
                    .orElse(null);
            if (subPlan != null) {
                school.setPlan(subPlan);
            }
        }

        if (dto.getOnboardingStep() != null) {
            school.setOnboardingStatus(dto.getOnboardingStep());
        } else if (school.getOnboardingStatus() == null) {
            school.setOnboardingStatus("LIVE");
        }
    }

    private SchoolDto toDto(School school) {
        int studentCount = (int) studentRepository.countBySchoolId(school.getId());
        int staffCount = (int) staffRepository.countBySchoolId(school.getId());
        int modulesCount = (int) accessRepository.findBySchoolId(school.getId()).stream()
                .filter(a -> Boolean.TRUE.equals(a.getEnabled()))
                .filter(a -> a.getModule() == null || a.getModule().getCode() == null
                        || !POST_MVP_DISABLED_MODULES.contains(a.getModule().getCode().toUpperCase()))
                .count();

        Optional<SchoolSubscription> activeSubOpt =
                subscriptionRepository.findFirstBySchoolIdAndStatusOrderByIdDesc(school.getId(), "ACTIVE");
        SchoolSubscription activeSub = activeSubOpt.orElse(null);

        Map<String, Object> metadata = school.getMetadata() != null ? school.getMetadata() : Map.of();

        String subdomain = school.getSubdomain() != null && !school.getSubdomain().isBlank()
                ? school.getSubdomain()
                : (school.getCode() != null ? school.getCode().toLowerCase() : "campus");
        String domain = subdomain + ".schooly.io";

        // Resolve canonical plan from active subscription -> school.plan -> metadata.subscriptionPlan
        String rawPlanHint = null;
        if (activeSub != null && activeSub.getPlan() != null) {
            rawPlanHint = firstNonBlank(activeSub.getPlan().getCode(), activeSub.getPlan().getName());
        }
        if (rawPlanHint == null && school.getPlan() != null) {
            rawPlanHint = firstNonBlank(school.getPlan().getCode(), school.getPlan().getName());
        }
        if (rawPlanHint == null && metadata.get("subscriptionPlan") != null) {
            rawPlanHint = metadata.get("subscriptionPlan").toString();
        }

        String planCode = canonicalPlanCode(rawPlanHint);
        String planName = canonicalPlanName(planCode, rawPlanHint);

        // Financials (BigDecimal precision) & billed students from real active subscription
        Integer billedStudentCount = activeSub != null && activeSub.getTotalStudents() != null
                ? activeSub.getTotalStudents()
                : 0;
        BigDecimal totalAmount = activeSub != null && activeSub.getTotalAmount() != null
                ? activeSub.getTotalAmount().setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        BigDecimal amountPaid = activeSub != null && activeSub.getAmountPaid() != null
                ? activeSub.getAmountPaid().setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        BigDecimal remainingAmount = activeSub != null && activeSub.getRemainingAmount() != null
                ? activeSub.getRemainingAmount().setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

        String paymentStatus;
        if (activeSub != null) {
            if (remainingAmount.compareTo(BigDecimal.ZERO) > 0 && amountPaid.compareTo(BigDecimal.ZERO) > 0) {
                paymentStatus = "PARTIAL";
            } else if (remainingAmount.compareTo(BigDecimal.ZERO) > 0) {
                paymentStatus = "PENDING";
            } else {
                paymentStatus = "PAID";
            }
        } else {
            paymentStatus = school.getPaymentStatus() != null ? school.getPaymentStatus().toUpperCase() : "PENDING";
        }

        Double monthlyAmount;
        if (totalAmount.compareTo(BigDecimal.ZERO) > 0) {
            monthlyAmount = totalAmount.divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP).doubleValue();
        } else {
            BigDecimal rate = BigDecimal.valueOf(defaultMonthlyRateForPlan(planCode));
            int basisStudents = billedStudentCount > 0 ? billedStudentCount : studentCount;
            monthlyAmount = rate.multiply(BigDecimal.valueOf(basisStudents)).doubleValue();
        }

        String renewalDate = null;
        if (activeSub != null && activeSub.getEndDate() != null) {
            renewalDate = activeSub.getEndDate().toString();
        } else if (school.getRenewalDate() != null) {
            renewalDate = school.getRenewalDate().toString();
        }

        String normalizedStatus = normalizeStatusForDisplay(school.getStatus());

        String healthStatus = "Healthy";
        if ("Suspended".equalsIgnoreCase(normalizedStatus) || "Inactive".equalsIgnoreCase(normalizedStatus)) {
            healthStatus = "Critical";
        } else if ("Draft".equalsIgnoreCase(normalizedStatus)) {
            healthStatus = "Warning";
        } else if ("PARTIAL".equalsIgnoreCase(paymentStatus) || "PENDING".equalsIgnoreCase(paymentStatus)) {
            healthStatus = "Due Pending";
        }

        String onboardingStep = school.getOnboardingStatus() != null ? school.getOnboardingStatus() : "Step 6: Activated";
        String createdAtStr = school.getCreatedAt() != null ? school.getCreatedAt().toString() : null;
        String boardType = metadata.get("boardType") != null ? metadata.get("boardType").toString() : null;
        String portalLoginPhone = firstNonBlank(
                metadata.get("portalLoginPhone") != null ? metadata.get("portalLoginPhone").toString() : null,
                school.getContactPhone()
        );

        return SchoolDto.builder()
                .id(school.getId())
                .name(school.getName())
                .code(school.getCode())
                .address(school.getAddress())
                .city(school.getCity())
                .state(school.getState())
                .logoUrl(school.getLogoUrl())
                .boardType(boardType)
                .contactEmail(school.getContactEmail())
                .contactPhone(school.getContactPhone())
                .portalLoginPhone(portalLoginPhone)
                .status(normalizedStatus)
                .subdomain(subdomain)
                .domain(domain)
                .plan(planName)
                .planCode(planCode)
                .studentCount(studentCount)
                .billedStudentCount(billedStudentCount)
                .staffCount(staffCount)
                .modulesCount(modulesCount)
                .healthStatus(healthStatus)
                .onboardingStep(onboardingStep)
                .createdAt(createdAtStr)
                .metadata(school.getMetadata())
                .monthlyAmount(monthlyAmount)
                .totalAmount(totalAmount)
                .amountPaid(amountPaid)
                .remainingAmount(remainingAmount)
                .paymentStatus(paymentStatus)
                .renewalDate(renewalDate)
                .build();
    }

    private String canonicalPlanCode(String raw) {
        if (raw == null || raw.isBlank()) return "BASIC";
        String u = raw.trim().toUpperCase();
        if (u.contains("ENTERPRISE") || u.contains("PREMIUM")) return "ENTERPRISE";
        if (u.contains("PRO") || u.contains("ADVANCED") || u.contains("STANDARD") || u.contains("GROWTH")) return "PRO";
        if (u.contains("BASIC") || u.contains("ESSENTIAL") || u.contains("FREE") || u.contains("STARTER")) return "BASIC";
        return null;
    }

    private String canonicalPlanName(String planCode, String fallbackRaw) {
        if ("ENTERPRISE".equals(planCode)) return "Premium Plan";
        if ("PRO".equals(planCode)) return "Advanced Plan";
        if ("BASIC".equals(planCode)) return "Essential Plan";
        return fallbackRaw != null && !fallbackRaw.isBlank() ? fallbackRaw : "Essential Plan";
    }

    private int defaultMonthlyRateForPlan(String planCode) {
        if ("ENTERPRISE".equals(planCode)) return 30;
        if ("PRO".equals(planCode)) return 20;
        return 10;
    }

    private String normalizeStatusForDisplay(String raw) {
        if (raw == null || raw.isBlank()) return "Active";
        String u = raw.trim().toUpperCase();
        if ("ACTIVE".equals(u)) return "Active";
        if ("SUSPENDED".equals(u)) return "Suspended";
        if ("INACTIVE".equals(u)) return "Inactive";
        if ("DRAFT".equals(u) || "PENDING".equals(u) || "ONBOARDING".equals(u)) return "Draft";
        return raw.trim();
    }

    private String normalizeStatusForStorage(String raw) {
        if (raw == null || raw.isBlank()) return "ACTIVE";
        String u = raw.trim().toUpperCase();
        if ("ACTIVE".equals(u) || "SUSPENDED".equals(u) || "INACTIVE".equals(u) || "DRAFT".equals(u)) {
            return u;
        }
        return raw.trim().toUpperCase();
    }

    private boolean containsIgnoreCase(String value, String queryLower) {
        return value != null && value.toLowerCase().contains(queryLower);
    }

    private String firstNonBlank(String... values) {
        for (String v : values) {
            if (v != null && !v.isBlank()) {
                return v.trim();
            }
        }
        return null;
    }
}
