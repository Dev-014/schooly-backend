package com.school.erp.controller;

import com.school.erp.api.ApiResponse;
import com.school.erp.dto.payment.FeeDueResponse;
import com.school.erp.dto.payment.PaymentResponse;
import com.school.erp.dto.student.StudentFeeSummaryResponse;
import com.school.erp.repository.StudentRepository;
import com.school.erp.security.AuthContextHolder;
import com.school.erp.security.AuthenticatedUser;
import com.school.erp.security.PermissionRequired;
import com.school.erp.service.FeeDueService;
import com.school.erp.service.FeePaymentService;
import com.school.erp.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Student self-service fee endpoints.
 * The authenticated student's identity is derived from the JWT — no explicit studentId in URL.
 */
@RestController
@RequestMapping("/api/v1/student/fees")
@RequiredArgsConstructor
public class StudentFeeController {

    private final FeeDueService feeDueService;
    private final FeePaymentService feePaymentService;
    private final StudentRepository studentRepository;

    @GetMapping("/dues")
    @PermissionRequired("fees.student_fees.view")
    public ResponseEntity<ApiResponse<List<FeeDueResponse>>> getMyDues() {
        AuthenticatedUser authUser = getAuthenticatedStudent();
        Long studentId = resolveStudentId(authUser);
        List<FeeDueResponse> dues = feeDueService.getStudentDues(studentId, authUser.schoolId(), null);
        return ResponseEntity.ok(ApiResponse.success(dues, "Fee dues fetched successfully"));
    }

    @GetMapping("/payments")
    @PermissionRequired("fees.student_fees.view")
    public ResponseEntity<ApiResponse<Page<PaymentResponse>>> getMyPayments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        AuthenticatedUser authUser = getAuthenticatedStudent();
        Long studentId = resolveStudentId(authUser);
        Page<PaymentResponse> payments = feePaymentService.getPaymentsByStudent(
                studentId, authUser.schoolId(), PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        );
        return ResponseEntity.ok(ApiResponse.success(payments, "Payment history fetched successfully"));
    }

    @GetMapping("/summary")
    @PermissionRequired("fees.student_fees.view")
    public ResponseEntity<ApiResponse<StudentFeeSummaryResponse>> getMyFeeSummary() {
        AuthenticatedUser authUser = getAuthenticatedStudent();
        Long studentId = resolveStudentId(authUser);
        List<FeeDueResponse> dues = feeDueService.getStudentDues(studentId, authUser.schoolId(), null);

        // FeeDueResponse uses BigDecimal for amount/paidAmount/discountAmount
        BigDecimal totalFee = dues.stream()
                .map(FeeDueResponse::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalPaid = dues.stream()
                .map(FeeDueResponse::paidAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalDiscount = dues.stream()
                .map(FeeDueResponse::discountAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal pending = totalFee.subtract(totalPaid).subtract(totalDiscount).max(BigDecimal.ZERO);

        String overallStatus;
        if (pending.compareTo(BigDecimal.ZERO) == 0) {
            overallStatus = "PAID";
        } else if (totalPaid.compareTo(BigDecimal.ZERO) > 0) {
            overallStatus = "PARTIALLY_PAID";
        } else {
            boolean hasOverdue = dues.stream().anyMatch(d -> "OVERDUE".equalsIgnoreCase(d.status()));
            overallStatus = hasOverdue ? "OVERDUE" : "PENDING";
        }

        Optional<String> nextDueDate = dues.stream()
                .filter(d -> !"PAID".equalsIgnoreCase(d.status()))
                .sorted(Comparator.comparing(FeeDueResponse::dueDate))
                .map(d -> d.dueDate().toString())
                .findFirst();

        StudentFeeSummaryResponse summary = new StudentFeeSummaryResponse(
                totalFee, totalPaid, pending, overallStatus, nextDueDate.orElse(null)
        );
        return ResponseEntity.ok(ApiResponse.success(summary, "Fee summary fetched successfully"));
    }

    private AuthenticatedUser getAuthenticatedStudent() {
        AuthenticatedUser authUser = AuthContextHolder.get();
        if (authUser == null) {
            throw new UnauthorizedException("Not authenticated");
        }
        return authUser;
    }

    private Long resolveStudentId(AuthenticatedUser authUser) {
        return studentRepository.findByUserId(authUser.userId())
                .stream()
                .filter(s -> s.getSchool() != null && s.getSchool().getId().equals(authUser.schoolId()))
                .findFirst()
                .orElseThrow(() -> new UnauthorizedException("No student record found for this user"))
                .getId();
    }
}
