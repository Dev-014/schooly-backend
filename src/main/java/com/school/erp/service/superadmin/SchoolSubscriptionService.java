package com.school.erp.service.superadmin;

import com.school.erp.dto.payment.GatewayOrderResponse;
import com.school.erp.dto.payment.GatewayVerificationResponse;
import com.school.erp.dto.superadmin.SchoolSubscriptionDTO;
import com.school.erp.dto.superadmin.SchoolSubscriptionInstallmentDTO;
import com.school.erp.dto.superadmin.SubscriptionOrderCreateRequest;
import com.school.erp.dto.superadmin.SubscriptionPaymentVerifyRequest;
import com.school.erp.entity.superadmin.*;
import com.school.erp.repository.superadmin.*;
import com.school.erp.service.finance.RazorpayGatewayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SchoolSubscriptionService {

    private final SchoolSubscriptionRepository subscriptionRepository;
    private final SchoolSubscriptionInstallmentRepository installmentRepository;
    private final SchoolRepository schoolRepository;
    private final SubscriptionPlanRepository planRepository;
    private final SchoolModuleAccessRepository moduleAccessRepository;
    private final RazorpayGatewayService razorpayGatewayService;

    @Transactional(readOnly = true)
    public SchoolSubscriptionDTO getActiveSubscriptionForSchool(Long schoolId) {
        SchoolSubscription subscription = subscriptionRepository.findFirstBySchoolIdAndStatusOrderByIdDesc(schoolId, "ACTIVE")
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, 
                        "Active subscription not found for school " + schoolId));

        return mapToDTO(subscription);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Track B: SaaS Subscription Order Creation (Master Razorpay Gateway)
    // ─────────────────────────────────────────────────────────────────────────
    @Transactional
    public GatewayOrderResponse createSubscriptionOrder(SubscriptionOrderCreateRequest request) {
        School school = schoolRepository.findById(request.schoolId())
                .orElseThrow(() -> new RuntimeException("School not found with ID: " + request.schoolId()));

        SubscriptionPlan plan = planRepository.findAllByStatus("ACTIVE").stream()
                .filter(p -> p.getName().equalsIgnoreCase(request.planName()) || p.getCode().equalsIgnoreCase(request.planName()))
                .findFirst()
                .orElseGet(() -> planRepository.findAll().stream().findFirst()
                        .orElseThrow(() -> new RuntimeException("No subscription plan available")));

        BigDecimal pricePerStudent = plan.getPricePerStudent() != null ? plan.getPricePerStudent() : BigDecimal.valueOf(20);
        int multiplier = switch (request.billingCycle().toUpperCase()) {
            case "QUARTERLY" -> 3;
            case "HALF_YEARLY" -> 6;
            default -> 12; // YEARLY
        };

        BigDecimal baseAmount = pricePerStudent.multiply(BigDecimal.valueOf(request.studentCount()))
                .multiply(BigDecimal.valueOf(multiplier));
        BigDecimal gstAmount = baseAmount.multiply(BigDecimal.valueOf(0.18)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal grandTotal = baseAmount.add(gstAmount);

        // Find or create pending subscription record
        SchoolSubscription sub = subscriptionRepository.findFirstBySchoolIdAndStatusOrderByIdDesc(school.getId(), "PENDING")
                .orElseGet(() -> {
                    SchoolSubscription s = new SchoolSubscription();
                    s.setSchool(school);
                    return s;
                });

        sub.setPlan(plan);
        sub.setBillingPeriod(request.billingCycle().toUpperCase());
        sub.setTotalStudents(request.studentCount());
        sub.setBaseAmount(baseAmount);
        sub.setGstRate(BigDecimal.valueOf(18.00));
        sub.setGstAmount(gstAmount);
        sub.setTotalAmount(grandTotal);
        sub.setAmountPaid(BigDecimal.ZERO);
        sub.setRemainingAmount(grandTotal);
        sub.setStatus("PENDING");

        sub = subscriptionRepository.save(sub);

        String orderId = razorpayGatewayService.createSubscriptionOrder(sub, school);
        sub.setGatewayOrderId(orderId);
        subscriptionRepository.save(sub);

        long amountInPaise = grandTotal.multiply(BigDecimal.valueOf(100)).longValue();

        return new GatewayOrderResponse(
                orderId,
                sub.getId(),
                grandTotal,
                amountInPaise,
                "INR",
                razorpayGatewayService.getKeyId(),
                school.getName(),
                "Subscription: " + plan.getName(),
                "SUB-" + sub.getId(),
                null,
                razorpayGatewayService.isSandbox()
        );
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Track B: SaaS Subscription Verification & Automatic Module Activation
    // ─────────────────────────────────────────────────────────────────────────
    @Transactional
    public GatewayVerificationResponse verifySubscriptionPayment(SubscriptionPaymentVerifyRequest request) {
        SchoolSubscription sub = subscriptionRepository.findFirstBySchoolIdAndStatusOrderByIdDesc(request.schoolId(), "PENDING")
                .orElseThrow(() -> new RuntimeException("No pending subscription found for school " + request.schoolId()));

        boolean isValid = razorpayGatewayService.verifySignature(
                request.razorpayOrderId(),
                request.razorpayPaymentId(),
                request.razorpaySignature()
        );

        if (!isValid) {
            log.error("Subscription payment signature verification failed for school {}", request.schoolId());
            return new GatewayVerificationResponse(false, sub.getId(), "SUB-" + sub.getId(), "FAILED", sub.getTotalAmount(), "Payment signature verification failed");
        }

        // Deactivate previous active subscriptions if any
        subscriptionRepository.findAllBySchoolIdAndStatus(request.schoolId(), "ACTIVE").forEach(prev -> {
            prev.setStatus("SUPERSEDED");
            subscriptionRepository.save(prev);
        });

        sub.setStatus("ACTIVE");
        sub.setGatewayPaymentId(request.razorpayPaymentId());
        sub.setAmountPaid(sub.getTotalAmount());
        sub.setRemainingAmount(BigDecimal.ZERO);
        sub.setStartDate(LocalDate.now());

        int months = switch (sub.getBillingPeriod().toUpperCase()) {
            case "QUARTERLY" -> 3;
            case "HALF_YEARLY" -> 6;
            default -> 12;
        };
        sub.setEndDate(LocalDate.now().plusMonths(months));
        sub = subscriptionRepository.save(sub);

        // Record 1st Installment as PAID
        SchoolSubscriptionInstallment inst1 = new SchoolSubscriptionInstallment();
        inst1.setSubscription(sub);
        inst1.setInstallmentNumber(1);
        inst1.setAmount(sub.getTotalAmount());
        inst1.setDueDate(LocalDate.now());
        inst1.setStatus("PAID");
        inst1.setPaidDate(LocalDate.now());
        installmentRepository.save(inst1);

        // Schedule future installments if quarterly / half-yearly
        if ("QUARTERLY".equalsIgnoreCase(sub.getBillingPeriod())) {
            for (int i = 2; i <= 4; i++) {
                SchoolSubscriptionInstallment pending = new SchoolSubscriptionInstallment();
                pending.setSubscription(sub);
                pending.setInstallmentNumber(i);
                pending.setAmount(sub.getTotalAmount());
                pending.setDueDate(LocalDate.now().plusMonths(3L * (i - 1)));
                pending.setStatus("PENDING");
                installmentRepository.save(pending);
            }
        }

        // Auto-provision entitlements/modules from plan
        if (sub.getPlan() != null && sub.getPlan().getModules() != null) {
            final School targetSchool = sub.getSchool();
            for (PlatformModule mod : sub.getPlan().getModules()) {
                SchoolModuleAccess access = moduleAccessRepository.findBySchoolIdAndModuleId(targetSchool.getId(), mod.getId())
                        .orElseGet(() -> {
                            SchoolModuleAccess a = new SchoolModuleAccess();
                            a.setSchool(targetSchool);
                            a.setModule(mod);
                            return a;
                        });
                access.setEnabled(true);
                moduleAccessRepository.save(access);
            }
            log.info("Provisioned {} plan modules for school {}", sub.getPlan().getModules().size(), targetSchool.getId());
        }

        return new GatewayVerificationResponse(
                true,
                sub.getId(),
                "SUB-" + sub.getId(),
                "ACTIVE",
                sub.getTotalAmount(),
                "Subscription activated successfully and modules provisioned."
        );
    }

    private SchoolSubscriptionDTO mapToDTO(SchoolSubscription subscription) {
        SchoolSubscriptionDTO dto = new SchoolSubscriptionDTO();
        dto.setId(subscription.getId());
        dto.setSchoolId(subscription.getSchool().getId());
        dto.setPlanName(subscription.getPlan() != null ? subscription.getPlan().getName() : "Standard");
        dto.setBillingPeriod(subscription.getBillingPeriod());
        dto.setTotalStudents(subscription.getTotalStudents());
        dto.setTotalAmount(subscription.getTotalAmount());
        dto.setAmountPaid(subscription.getAmountPaid());
        dto.setRemainingAmount(subscription.getRemainingAmount());
        dto.setStatus(subscription.getStatus());
        dto.setStartDate(subscription.getStartDate());
        dto.setEndDate(subscription.getEndDate());

        List<SchoolSubscriptionInstallmentDTO> installmentDTOs = subscription.getInstallments().stream()
                .map(this::mapInstallmentToDTO)
                .collect(Collectors.toList());
        dto.setInstallments(installmentDTOs);

        return dto;
    }

    private SchoolSubscriptionInstallmentDTO mapInstallmentToDTO(SchoolSubscriptionInstallment installment) {
        SchoolSubscriptionInstallmentDTO dto = new SchoolSubscriptionInstallmentDTO();
        dto.setId(installment.getId());
        dto.setInstallmentNumber(installment.getInstallmentNumber());
        dto.setAmount(installment.getAmount());
        dto.setDueDate(installment.getDueDate());
        dto.setStatus(installment.getStatus());
        dto.setPaidDate(installment.getPaidDate());
        return dto;
    }
}
