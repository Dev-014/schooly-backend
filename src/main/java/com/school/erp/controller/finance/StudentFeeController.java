package com.school.erp.controller.finance;

import com.school.erp.api.ApiResponse;
import com.school.erp.dto.payment.FeeDueResponse;
import com.school.erp.dto.payment.PaymentResponse;
import com.school.erp.dto.student.StudentFeeSummaryResponse;
import com.school.erp.entity.auth.UserRole;
import com.school.erp.entity.student.StudentParent;
import com.school.erp.repository.student.StudentParentRepository;
import com.school.erp.repository.student.StudentRepository;
import com.school.erp.security.AuthContextHolder;
import com.school.erp.security.AuthenticatedUser;
import com.school.erp.security.PermissionRequired;
import com.school.erp.service.finance.FeeDueService;
import com.school.erp.service.finance.FeePaymentService;
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
 * Unified Student & Parent fee endpoints.
 * Auto-resolves identity from JWT for students, and allows parents to access linked child records.
 */
@RestController
@RequestMapping("/api/v1/student/fees")
@RequiredArgsConstructor
public class StudentFeeController {

    private final FeeDueService feeDueService;
    private final FeePaymentService feePaymentService;
    private final StudentRepository studentRepository;
    private final StudentParentRepository studentParentRepository;

    @GetMapping("/dues")
    @PermissionRequired("fees.student_fees.view")
    public ResponseEntity<ApiResponse<List<FeeDueResponse>>> getMyDues(
            @RequestParam(required = false) Long studentId) {
        ResolvedFeeContext ctx = resolveTargetStudent(studentId);
        List<FeeDueResponse> dues = feeDueService.getStudentDues(ctx.studentId(), ctx.schoolId(), null);
        return ResponseEntity.ok(ApiResponse.success(dues, "Fee dues fetched successfully"));
    }

    @GetMapping("/payments")
    @PermissionRequired("fees.student_fees.view")
    public ResponseEntity<ApiResponse<Page<PaymentResponse>>> getMyPayments(
            @RequestParam(required = false) Long studentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        ResolvedFeeContext ctx = resolveTargetStudent(studentId);
        Page<PaymentResponse> payments = feePaymentService.getPaymentsByStudent(
                ctx.studentId(), ctx.schoolId(), PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        );
        return ResponseEntity.ok(ApiResponse.success(payments, "Payment history fetched successfully"));
    }

    @GetMapping("/summary")
    @PermissionRequired("fees.student_fees.view")
    public ResponseEntity<ApiResponse<StudentFeeSummaryResponse>> getMyFeeSummary(
            @RequestParam(required = false) Long studentId) {
        ResolvedFeeContext ctx = resolveTargetStudent(studentId);
        List<FeeDueResponse> dues = feeDueService.getStudentDues(ctx.studentId(), ctx.schoolId(), null);

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

    private record ResolvedFeeContext(Long studentId, Long schoolId) {}

    private ResolvedFeeContext resolveTargetStudent(Long requestedStudentId) {
        AuthenticatedUser authUser = AuthContextHolder.get();
        if (authUser == null) {
            throw new UnauthorizedException("Not authenticated");
        }

        if (authUser.role() == UserRole.PARENT) {
            List<StudentParent> relations = studentParentRepository.findByIdParentUserId(authUser.userId());
            if (relations.isEmpty()) {
                throw new UnauthorizedException("No children linked to parent profile");
            }
            StudentParent matched;
            if (requestedStudentId != null) {
                matched = relations.stream()
                        .filter(sp -> sp.getStudent().getId().equals(requestedStudentId))
                        .findFirst()
                        .orElseThrow(() -> new UnauthorizedException("You do not have access to this student's records"));
            } else {
                matched = relations.get(0);
            }
            Long schoolId = matched.getStudent().getSchool() != null ? matched.getStudent().getSchool().getId() : authUser.schoolId();
            return new ResolvedFeeContext(matched.getStudent().getId(), schoolId);
        }

        if (authUser.role() == UserRole.STUDENT) {
            Long studentId = studentRepository.findByUserId(authUser.userId())
                    .stream()
                    .filter(s -> s.getSchool() != null && s.getSchool().getId().equals(authUser.schoolId()))
                    .findFirst()
                    .orElseThrow(() -> new UnauthorizedException("No student record found for this user"))
                    .getId();
            return new ResolvedFeeContext(studentId, authUser.schoolId());
        }

        // Staff / Admin viewing a student's fees
        if (requestedStudentId != null) {
            return new ResolvedFeeContext(requestedStudentId, authUser.schoolId());
        }

        throw new IllegalArgumentException("studentId parameter is required for staff or admin roles");
    }
}
