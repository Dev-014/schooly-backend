package com.school.erp.service.finance.impl;

import com.school.erp.dto.payment.*;
import com.school.erp.entity.academic.Section;
import com.school.erp.entity.finance.FeeDue;
import com.school.erp.entity.finance.FeePaymentAllocation;
import com.school.erp.entity.finance.GatewayTransactionLog;
import com.school.erp.entity.finance.Payment;
import com.school.erp.entity.finance.SchoolAccount;
import com.school.erp.entity.notification.NotificationPriority;
import com.school.erp.entity.notification.NotificationType;
import com.school.erp.entity.student.Student;
import com.school.erp.entity.superadmin.School;
import com.school.erp.repository.academic.SectionRepository;
import com.school.erp.repository.finance.FeeDueRepository;
import com.school.erp.repository.finance.FeePaymentAllocationRepository;
import com.school.erp.repository.finance.GatewayTransactionLogRepository;
import com.school.erp.repository.finance.PaymentRepository;
import com.school.erp.repository.finance.SchoolAccountRepository;
import com.school.erp.repository.student.StudentRepository;
import com.school.erp.repository.superadmin.SchoolRepository;
import com.school.erp.service.finance.FeePaymentService;
import com.school.erp.service.finance.RazorpayGatewayService;
import com.school.erp.service.notification.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
public class FeePaymentServiceImpl implements FeePaymentService {

    private final PaymentRepository paymentRepository;
    private final FeeDueRepository feeDueRepository;
    private final FeePaymentAllocationRepository feePaymentAllocationRepository;
    private final StudentRepository studentRepository;
    private final SchoolRepository schoolRepository;
    private final SectionRepository sectionRepository;
    private final SchoolAccountRepository schoolAccountRepository;
    private final GatewayTransactionLogRepository gatewayTransactionLogRepository;
    private final RazorpayGatewayService razorpayGatewayService;

    @Autowired(required = false)
    private NotificationService notificationService;

    public FeePaymentServiceImpl(
            PaymentRepository paymentRepository,
            FeeDueRepository feeDueRepository,
            FeePaymentAllocationRepository feePaymentAllocationRepository,
            StudentRepository studentRepository,
            SchoolRepository schoolRepository,
            SectionRepository sectionRepository,
            SchoolAccountRepository schoolAccountRepository,
            GatewayTransactionLogRepository gatewayTransactionLogRepository,
            RazorpayGatewayService razorpayGatewayService) {
        this.paymentRepository = paymentRepository;
        this.feeDueRepository = feeDueRepository;
        this.feePaymentAllocationRepository = feePaymentAllocationRepository;
        this.studentRepository = studentRepository;
        this.schoolRepository = schoolRepository;
        this.sectionRepository = sectionRepository;
        this.schoolAccountRepository = schoolAccountRepository;
        this.gatewayTransactionLogRepository = gatewayTransactionLogRepository;
        this.razorpayGatewayService = razorpayGatewayService;
    }

    @Override
    public PaymentResponse processPayment(FeePaymentRequest request) {
        School school = schoolRepository.findById(request.getSchoolId())
                .orElseThrow(() -> new RuntimeException("School not found"));
        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new RuntimeException("Student not found"));

        Payment payment = new Payment();
        payment.setSchool(school);
        payment.setStudent(student);
        payment.setAmount(request.getTotalAmount());
        payment.setUnallocatedAmount(request.getTotalAmount());
        payment.setPaymentMode(request.getPaymentMethod());
        payment.setPaymentDate(request.getPaymentDate() != null ? request.getPaymentDate() : LocalDate.now());
        payment.setStatus("SUCCESS");
        payment.setVerificationStatus("APPROVED");
        payment.setReceiptNumber("REC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());

        payment = paymentRepository.save(payment);

        List<PaymentResponse.PaymentAllocationResponse> allocations = allocatePaymentToDues(payment, request.getFeeDueIds());

        sendPaymentSuccessNotifications(school, student, payment);

        return mapToPaymentResponse(payment, student, allocations);
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // Track A: Online Gateway Order Creation (Razorpay Route)
    // ─────────────────────────────────────────────────────────────────────────────
    @Override
    public GatewayOrderResponse createOnlineOrder(GatewayOrderCreateRequest request) {
        Long schoolId = request.schoolId();
        Student student = studentRepository.findById(request.studentId())
                .orElseThrow(() -> new RuntimeException("Student not found with ID: " + request.studentId()));

        if (schoolId == null && student.getSchool() != null) {
            schoolId = student.getSchool().getId();
        }
        School school = schoolRepository.findById(schoolId)
                .orElseThrow(() -> new RuntimeException("School not found"));

        // Lookup School's Razorpay Route Linked Account
        String routeAccountId = schoolAccountRepository.findFirstBySchoolIdAndIsActiveTrue(school.getId())
                .map(SchoolAccount::getRazorpayAccountId)
                .orElse(null);

        // Pre-create internal payment entity in PENDING state
        Payment payment = new Payment();
        payment.setSchool(school);
        payment.setStudent(student);
        payment.setAmount(request.totalAmount());
        payment.setUnallocatedAmount(request.totalAmount());
        payment.setPaymentMode("ONLINE");
        payment.setPaymentDate(LocalDate.now());
        payment.setStatus("PENDING");
        payment.setVerificationStatus("APPROVED");
        payment.setReceiptNumber("REC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        payment.setCurrency(request.currency() != null ? request.currency() : "INR");

        payment = paymentRepository.save(payment);

        // Call Razorpay Gateway Service
        String orderId = razorpayGatewayService.createStudentFeeOrder(payment, student, school, routeAccountId);
        payment.setGatewayOrderId(orderId);
        paymentRepository.save(payment);

        // Audit Log
        logGatewayEvent("STUDENT_FEE", payment.getId(), school, "ORDER_CREATED", orderId, null, payment.getAmount(), "PENDING", null);

        long amountInPaise = request.totalAmount().multiply(BigDecimal.valueOf(100)).longValue();
        String studentDisplayName = student.getFirstName() + (student.getLastName() != null ? " " + student.getLastName() : "");

        return new GatewayOrderResponse(
                orderId,
                payment.getId(),
                payment.getAmount(),
                amountInPaise,
                payment.getCurrency(),
                razorpayGatewayService.getKeyId(),
                school.getName(),
                studentDisplayName,
                payment.getReceiptNumber(),
                routeAccountId,
                razorpayGatewayService.isSandbox()
        );
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // Track A: Online Gateway Verification & Dues Allocation
    // ─────────────────────────────────────────────────────────────────────────────
    @Override
    public GatewayVerificationResponse verifyOnlinePayment(GatewayPaymentVerifyRequest request) {
        Payment payment = paymentRepository.findById(request.internalPaymentId())
                .orElseThrow(() -> new RuntimeException("Payment record not found for id: " + request.internalPaymentId()));

        // Idempotency: if already settled, return success immediately
        if ("SUCCESS".equalsIgnoreCase(payment.getStatus())) {
            return new GatewayVerificationResponse(
                    true,
                    payment.getId(),
                    payment.getReceiptNumber(),
                    "SUCCESS",
                    payment.getAmount(),
                    "Payment was already verified and settled."
            );
        }

        boolean isValid = razorpayGatewayService.verifySignature(
                request.razorpayOrderId(),
                request.razorpayPaymentId(),
                request.razorpaySignature()
        );

        if (!isValid) {
            payment.setStatus("FAILED");
            payment.setFailureReason("Digital signature verification failed");
            paymentRepository.save(payment);

            logGatewayEvent("STUDENT_FEE", payment.getId(), payment.getSchool(), "VERIFICATION_FAILED",
                    request.razorpayOrderId(), request.razorpayPaymentId(), payment.getAmount(), "FAILED", null);

            return new GatewayVerificationResponse(
                    false,
                    payment.getId(),
                    payment.getReceiptNumber(),
                    "FAILED",
                    payment.getAmount(),
                    "Payment signature verification failed."
            );
        }

        // Signature Verified: Settle payment and perform waterfall allocation
        payment.setStatus("SUCCESS");
        payment.setGatewayPaymentId(request.razorpayPaymentId());
        payment.setGatewaySignature(request.razorpaySignature());
        payment.setTransactionId(request.razorpayPaymentId());
        payment = paymentRepository.save(payment);

        // Fetch student's unpaid dues in priority order
        List<FeeDue> studentDues = feeDueRepository.findByStudentIdAndSchoolIdOrderByDueDateAsc(
                payment.getStudent().getId(), payment.getSchool().getId()
        );
        List<Long> eligibleDueIds = studentDues.stream()
                .filter(d -> !"PAID".equalsIgnoreCase(d.getStatus()))
                .map(FeeDue::getId)
                .toList();

        allocatePaymentToDues(payment, eligibleDueIds);

        sendPaymentSuccessNotifications(payment.getSchool(), payment.getStudent(), payment);

        logGatewayEvent("STUDENT_FEE", payment.getId(), payment.getSchool(), "PAYMENT_SETTLED",
                request.razorpayOrderId(), request.razorpayPaymentId(), payment.getAmount(), "SUCCESS", null);

        return new GatewayVerificationResponse(
                true,
                payment.getId(),
                payment.getReceiptNumber(),
                "SUCCESS",
                payment.getAmount(),
                "Payment successfully verified and settled."
        );
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // Track A: Offline Hybrid Payment Submission (with Document/Receipt Proof)
    // ─────────────────────────────────────────────────────────────────────────────
    @Override
    public PaymentResponse submitOfflinePayment(OfflinePaymentSubmissionRequest request, Long submittedByUserId) {
        Long schoolId = request.schoolId();
        Student student = studentRepository.findById(request.studentId())
                .orElseThrow(() -> new RuntimeException("Student not found"));

        if (schoolId == null && student.getSchool() != null) {
            schoolId = student.getSchool().getId();
        }
        School school = schoolRepository.findById(schoolId)
                .orElseThrow(() -> new RuntimeException("School not found"));

        boolean isCashierDirect = "CASH".equalsIgnoreCase(request.paymentMode()) && submittedByUserId != null;

        Payment payment = new Payment();
        payment.setSchool(school);
        payment.setStudent(student);
        payment.setAmount(request.totalAmount());
        payment.setUnallocatedAmount(request.totalAmount());
        payment.setPaymentMode(request.paymentMode().toUpperCase());
        payment.setPaymentDate(request.paymentDate() != null ? request.paymentDate() : LocalDate.now());
        payment.setTransactionId(request.referenceNumber());
        payment.setReceiptNumber("REC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        payment.setReceiptFileId(request.receiptFileId());
        payment.setVerificationNote(request.note());

        if (isCashierDirect) {
            // Cash counter payments collected by staff are instantly approved
            payment.setStatus("SUCCESS");
            payment.setVerificationStatus("APPROVED");
            payment.setVerifiedBy(submittedByUserId);
            payment.setVerifiedAt(LocalDateTime.now());
            payment = paymentRepository.save(payment);

            List<PaymentResponse.PaymentAllocationResponse> allocations = allocatePaymentToDues(payment, request.feeDueIds());
            sendPaymentSuccessNotifications(school, student, payment);
            return mapToPaymentResponse(payment, student, allocations);
        } else {
            // Cheques, NEFTs, or parent offline uploads enter verification queue
            payment.setStatus("PENDING");
            payment.setVerificationStatus("PENDING_VERIFICATION");
            payment = paymentRepository.save(payment);

            log.info("Offline payment of ₹{} submitted for student id={}, awaiting accountant verification",
                    payment.getAmount(), student.getId());
            return mapToPaymentResponse(payment, student, Collections.emptyList());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // Track A: Offline Accountant Verification Queue
    // ─────────────────────────────────────────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public Page<PaymentResponse> getPendingVerifications(Long schoolId, Pageable pageable) {
        Page<Payment> pendingList = paymentRepository.findBySchoolIdAndVerificationStatusOrderByCreatedAtDesc(
                schoolId, "PENDING_VERIFICATION", pageable
        );

        return pendingList.map(payment -> {
            Student s = payment.getStudent();
            String studentName = s != null ? (s.getFirstName() + " " + (s.getLastName() != null ? s.getLastName() : "")) : "Student";
            String className = (s != null && s.getSchoolClass() != null) ? s.getSchoolClass().getName() : "";

            return new PaymentResponse(
                    payment.getId(),
                    payment.getSchool().getId(),
                    payment.getAmount(),
                    payment.getPaymentMode(),
                    payment.getTransactionId(),
                    payment.getStatus(),
                    payment.getReceiptNumber(),
                    studentName,
                    className,
                    payment.getPaymentDate(),
                    payment.getCreatedAt(),
                    Collections.emptyList(),
                    payment.getVerificationStatus(),
                    payment.getVerificationNote(),
                    payment.getReceiptFileId()
            );
        });
    }

    @Override
    public PaymentResponse verifyOfflinePayment(Long paymentId, OfflinePaymentVerificationRequest request, Long verifiedByUserId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found for id: " + paymentId));

        if (!"PENDING_VERIFICATION".equalsIgnoreCase(payment.getVerificationStatus())) {
            throw new IllegalStateException("Payment is not in PENDING_VERIFICATION state (current: " + payment.getVerificationStatus() + ")");
        }

        payment.setVerifiedBy(verifiedByUserId);
        payment.setVerifiedAt(LocalDateTime.now());
        if (request.verificationNote() != null) {
            payment.setVerificationNote(request.verificationNote());
        }

        if (Boolean.TRUE.equals(request.approved())) {
            payment.setVerificationStatus("APPROVED");
            payment.setStatus("SUCCESS");
            payment = paymentRepository.save(payment);

            // Fetch unpaid dues and allocate
            List<FeeDue> studentDues = feeDueRepository.findByStudentIdAndSchoolIdOrderByDueDateAsc(
                    payment.getStudent().getId(), payment.getSchool().getId()
            );
            List<Long> eligibleDueIds = studentDues.stream()
                    .filter(d -> !"PAID".equalsIgnoreCase(d.getStatus()))
                    .map(FeeDue::getId)
                    .toList();

            List<PaymentResponse.PaymentAllocationResponse> allocations = allocatePaymentToDues(payment, eligibleDueIds);
            sendPaymentSuccessNotifications(payment.getSchool(), payment.getStudent(), payment);

            return mapToPaymentResponse(payment, payment.getStudent(), allocations);
        } else {
            payment.setVerificationStatus("REJECTED");
            payment.setStatus("REJECTED");
            payment.setFailureReason(request.verificationNote() != null ? request.verificationNote() : "Offline payment proof rejected by accountant");
            payment = paymentRepository.save(payment);

            log.warn("Offline payment id={} was REJECTED by user id={}: {}", paymentId, verifiedByUserId, payment.getFailureReason());
            return mapToPaymentResponse(payment, payment.getStudent(), Collections.emptyList());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // Webhook Event Processing
    // ─────────────────────────────────────────────────────────────────────────────
    @Override
    public void processWebhookEvent(String payload, String signature) {
        boolean valid = razorpayGatewayService.verifyWebhookSignature(payload, signature);
        if (!valid) {
            log.warn("Unauthorized webhook attempt rejected due to invalid signature");
            throw new SecurityException("Invalid webhook signature");
        }

        try {
            JSONObject eventJson = new JSONObject(payload);
            String event = eventJson.optString("event");
            log.info("Processing Razorpay webhook event: {}", event);

            if ("payment.captured".equals(event)) {
                JSONObject payloadObj = eventJson.optJSONObject("payload");
                if (payloadObj != null) {
                    JSONObject paymentObj = payloadObj.optJSONObject("payment");
                    if (paymentObj != null) {
                        JSONObject entity = paymentObj.optJSONObject("entity");
                        if (entity != null) {
                            String orderId = entity.optString("order_id");
                            String gatewayPaymentId = entity.optString("id");

                            if (orderId != null && !orderId.isBlank()) {
                                paymentRepository.findByGatewayOrderId(orderId).ifPresent(p -> {
                                    if ("PENDING".equalsIgnoreCase(p.getStatus())) {
                                        p.setStatus("SUCCESS");
                                        p.setGatewayPaymentId(gatewayPaymentId);
                                        p.setTransactionId(gatewayPaymentId);
                                        paymentRepository.save(p);

                                        List<FeeDue> dues = feeDueRepository.findByStudentIdAndSchoolIdOrderByDueDateAsc(
                                                p.getStudent().getId(), p.getSchool().getId()
                                        );
                                        List<Long> dueIds = dues.stream().filter(d -> !"PAID".equals(d.getStatus())).map(FeeDue::getId).toList();
                                        allocatePaymentToDues(p, dueIds);
                                        sendPaymentSuccessNotifications(p.getSchool(), p.getStudent(), p);
                                        log.info("Webhook auto-reconciled student fee payment id={}", p.getId());
                                    }
                                });
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error processing webhook payload: {}", e.getMessage(), e);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // Helper: Shared Waterfall Allocation Engine
    // ─────────────────────────────────────────────────────────────────────────────
    private List<PaymentResponse.PaymentAllocationResponse> allocatePaymentToDues(Payment payment, List<Long> feeDueIds) {
        BigDecimal remainingToAllocate = payment.getUnallocatedAmount() != null ? payment.getUnallocatedAmount() : payment.getAmount();
        List<PaymentResponse.PaymentAllocationResponse> allocations = new ArrayList<>();

        if (feeDueIds != null && !feeDueIds.isEmpty()) {
            List<FeeDue> dues = feeDueRepository.findAllById(feeDueIds);
            dues.sort(Comparator.comparing(FeeDue::getDueDate));

            for (FeeDue due : dues) {
                if (remainingToAllocate.compareTo(BigDecimal.ZERO) <= 0) break;
                if ("PAID".equals(due.getStatus())) continue;

                BigDecimal discount = due.getDiscountAmount() != null ? due.getDiscountAmount() : BigDecimal.ZERO;
                BigDecimal paid = due.getPaidAmount() != null ? due.getPaidAmount() : BigDecimal.ZERO;
                BigDecimal amountNeeded = due.getAmount().subtract(paid).subtract(discount);

                if (amountNeeded.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal allocated = amountNeeded.min(remainingToAllocate);
                    BigDecimal newPaid = paid.add(allocated);
                    due.setPaidAmount(newPaid);

                    if (newPaid.add(discount).compareTo(due.getAmount()) >= 0) {
                        due.setStatus("PAID");
                    } else if (newPaid.compareTo(BigDecimal.ZERO) > 0) {
                        due.setStatus("PARTIAL");
                    } else {
                        due.setStatus("UNPAID");
                    }
                    feeDueRepository.save(due);

                    FeePaymentAllocation allocation = new FeePaymentAllocation();
                    allocation.setPayment(payment);
                    allocation.setFeeDue(due);
                    allocation.setAllocatedAmount(allocated);
                    feePaymentAllocationRepository.save(allocation);

                    allocations.add(new PaymentResponse.PaymentAllocationResponse(due.getId(), due.getTitle(), allocated));
                    remainingToAllocate = remainingToAllocate.subtract(allocated);
                } else if (paid.add(discount).compareTo(due.getAmount()) >= 0) {
                    due.setStatus("PAID");
                    feeDueRepository.save(due);
                }
            }

            payment.setUnallocatedAmount(remainingToAllocate);
            paymentRepository.save(payment);
        }

        return allocations;
    }

    private void sendPaymentSuccessNotifications(School school, Student student, Payment payment) {
        if (notificationService == null) return;
        try {
            notificationService.notifyStudentAndParents(
                    school.getId(),
                    student,
                    NotificationType.FEE_PAYMENT_SUCCESS,
                    NotificationPriority.MEDIUM,
                    "Fee Payment Successful (" + payment.getReceiptNumber() + ")",
                    "Payment of ₹" + payment.getAmount() + " for " + student.getName() + " was received via " + payment.getPaymentMode() + ". Receipt: " + payment.getReceiptNumber() + ".",
                    "/finance/payments",
                    "Download Receipt",
                    "FEE_PAYMENT",
                    String.valueOf(payment.getId()),
                    Map.of(
                            "paymentId", payment.getId(),
                            "receiptNumber", payment.getReceiptNumber(),
                            "amount", payment.getAmount(),
                            "studentId", student.getId()
                    )
            );

            notificationService.notifySchoolAdmins(
                    school.getId(),
                    NotificationType.FEE_PAYMENT_SUCCESS,
                    NotificationPriority.LOW,
                    "Fee Payment Received: " + student.getName(),
                    "Fee payment of ₹" + payment.getAmount() + " received for " + student.getName() + " (Receipt: " + payment.getReceiptNumber() + ").",
                    "/finance/payments",
                    "View Payment",
                    "FEE_PAYMENT",
                    String.valueOf(payment.getId()),
                    Map.of(
                            "paymentId", payment.getId(),
                            "receiptNumber", payment.getReceiptNumber(),
                            "amount", payment.getAmount(),
                            "studentId", student.getId()
                    )
            );
        } catch (Exception e) {
            log.error("Failed to deliver fee payment notification: {}", e.getMessage());
        }
    }

    private void logGatewayEvent(String domain, Long refId, School school, String eventType, String orderId, String paymentId, BigDecimal amount, String status, String rawPayload) {
        try {
            GatewayTransactionLog gtl = new GatewayTransactionLog();
            gtl.setDomain(domain);
            gtl.setReferenceId(refId);
            gtl.setSchool(school);
            gtl.setGateway("RAZORPAY");
            gtl.setEventType(eventType);
            gtl.setGatewayOrderId(orderId);
            gtl.setGatewayPaymentId(paymentId);
            gtl.setAmount(amount);
            gtl.setStatus(status);
            gtl.setRawPayload(rawPayload);
            gatewayTransactionLogRepository.save(gtl);
        } catch (Exception e) {
            log.error("Failed to write gateway audit log: {}", e.getMessage());
        }
    }

    private PaymentResponse mapToPaymentResponse(Payment payment, Student student, List<PaymentResponse.PaymentAllocationResponse> allocations) {
        String studentName = student != null ? (student.getFirstName() + " " + (student.getLastName() != null ? student.getLastName() : "")) : "Student";
        String className = (student != null && student.getSchoolClass() != null) ? student.getSchoolClass().getName() : "";

        return new PaymentResponse(
                payment.getId(),
                payment.getSchool().getId(),
                payment.getAmount(),
                payment.getPaymentMode(),
                payment.getTransactionId(),
                payment.getStatus(),
                payment.getReceiptNumber(),
                studentName,
                className,
                payment.getPaymentDate(),
                payment.getCreatedAt(),
                allocations,
                payment.getVerificationStatus(),
                payment.getVerificationNote(),
                payment.getReceiptFileId()
        );
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // Query Methods
    // ─────────────────────────────────────────────────────────────────────────────
    @Override
    public Page<PaymentResponse> getPaymentsBySchool(Long schoolId, String search, Pageable pageable) {
        Page<Payment> payments;
        if (search != null && !search.trim().isEmpty()) {
            payments = paymentRepository.findBySchoolIdAndSearch(schoolId, search, pageable);
        } else {
            payments = paymentRepository.findBySchoolIdOrderByCreatedAtDescIdDesc(schoolId, pageable);
        }

        List<Long> paymentIds = payments.getContent().stream().map(Payment::getId).toList();
        Map<Long, List<FeePaymentAllocation>> allocMap = paymentIds.isEmpty()
                ? Collections.emptyMap()
                : feePaymentAllocationRepository.findByPaymentIdIn(paymentIds).stream()
                .collect(Collectors.groupingBy(a -> a.getPayment().getId()));

        return payments.map(payment -> {
            List<PaymentResponse.PaymentAllocationResponse> allocations = allocMap
                    .getOrDefault(payment.getId(), Collections.emptyList())
                    .stream()
                    .map(alloc -> new PaymentResponse.PaymentAllocationResponse(
                            alloc.getFeeDue().getId(),
                            alloc.getFeeDue().getTitle(),
                            alloc.getAllocatedAmount()
                    )).toList();

            Student student = payment.getStudent();
            String studentName = "";
            if (student != null) {
                if (student.getFirstName() != null) {
                    studentName = student.getFirstName() + (student.getLastName() != null && !student.getLastName().isBlank() ? " " + student.getLastName() : "");
                } else if (student.getName() != null && !student.getName().isBlank()) {
                    studentName = student.getName();
                } else {
                    studentName = "Student " + (student.getAdmissionNo() != null ? student.getAdmissionNo() : student.getId());
                }
            }

            return new PaymentResponse(
                    payment.getId(),
                    payment.getSchool().getId(),
                    payment.getAmount(),
                    payment.getPaymentMode(),
                    payment.getTransactionId(),
                    payment.getStatus(),
                    payment.getReceiptNumber(),
                    studentName,
                    (student != null && student.getSchoolClass() != null) ? student.getSchoolClass().getName() : "",
                    payment.getPaymentDate(),
                    payment.getCreatedAt(),
                    allocations,
                    payment.getVerificationStatus(),
                    payment.getVerificationNote(),
                    payment.getReceiptFileId()
            );
        });
    }

    @Override
    public Page<PaymentResponse> getPaymentsByStudent(Long studentId, Long schoolId, Pageable pageable) {
        Page<Payment> payments = paymentRepository.findBySchoolIdAndStudentId(schoolId, studentId, pageable);

        return payments.map(payment -> {
            List<PaymentResponse.PaymentAllocationResponse> allocations = feePaymentAllocationRepository.findByPaymentId(payment.getId())
                    .stream()
                    .map(alloc -> new PaymentResponse.PaymentAllocationResponse(
                            alloc.getFeeDue().getId(),
                            alloc.getFeeDue().getTitle(),
                            alloc.getAllocatedAmount()
                    )).toList();

            Student student = payment.getStudent();
            String studentName = "";
            if (student != null) {
                if (student.getFirstName() != null) {
                    studentName = student.getFirstName() + (student.getLastName() != null && !student.getLastName().isBlank() ? " " + student.getLastName() : "");
                } else if (student.getName() != null && !student.getName().isBlank()) {
                    studentName = student.getName();
                } else {
                    studentName = "Student " + (student.getAdmissionNo() != null ? student.getAdmissionNo() : student.getId());
                }
            }

            return new PaymentResponse(
                    payment.getId(),
                    payment.getSchool().getId(),
                    payment.getAmount(),
                    payment.getPaymentMode(),
                    payment.getTransactionId(),
                    payment.getStatus(),
                    payment.getReceiptNumber(),
                    studentName,
                    (student != null && student.getSchoolClass() != null) ? student.getSchoolClass().getName() : "",
                    payment.getPaymentDate(),
                    payment.getCreatedAt(),
                    allocations,
                    payment.getVerificationStatus(),
                    payment.getVerificationNote(),
                    payment.getReceiptFileId()
            );
        });
    }
}
