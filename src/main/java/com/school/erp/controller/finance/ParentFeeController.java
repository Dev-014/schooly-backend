package com.school.erp.controller;

import com.school.erp.api.ApiResponse;
import com.school.erp.dto.parent.ParentChildResponse;
import com.school.erp.dto.payment.FeeDueResponse;
import com.school.erp.dto.payment.PaymentResponse;
import com.school.erp.dto.student.StudentFeeSummaryResponse;
import com.school.erp.security.AuthContextHolder;
import com.school.erp.security.AuthenticatedUser;
import com.school.erp.security.PermissionRequired;
import com.school.erp.service.FeeDueService;
import com.school.erp.service.FeePaymentService;
import com.school.erp.service.ParentService;
import com.school.erp.repository.StudentParentRepository;
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

@RestController
@RequestMapping("/api/v1/parent/fees")
@RequiredArgsConstructor
public class ParentFeeController {

    private final FeeDueService feeDueService;
    private final FeePaymentService feePaymentService;
    private final ParentService parentService;
    private final StudentParentRepository studentParentRepository;



    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<StudentFeeSummaryResponse>> getChildFeeSummary(@RequestParam Long studentId) {
        AuthenticatedUser authUser = getAuthenticatedParent();
        Long resolvedSchoolId = validateParentAccessToChildAndGetSchoolId(authUser.userId(), studentId);

        List<FeeDueResponse> dues = feeDueService.getStudentDues(studentId, resolvedSchoolId, null);

        BigDecimal totalFee = dues.stream().map(FeeDueResponse::amount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalPaid = dues.stream().map(FeeDueResponse::paidAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalDiscount = dues.stream().map(FeeDueResponse::discountAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

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

    @GetMapping("/dues")
    public ResponseEntity<ApiResponse<List<FeeDueResponse>>> getChildDues(@RequestParam Long studentId) {
        AuthenticatedUser authUser = getAuthenticatedParent();
        Long resolvedSchoolId = validateParentAccessToChildAndGetSchoolId(authUser.userId(), studentId);

        List<FeeDueResponse> dues = feeDueService.getStudentDues(studentId, resolvedSchoolId, null);
        return ResponseEntity.ok(ApiResponse.success(dues, "Fee dues fetched successfully"));
    }

    @GetMapping("/payments")
    public ResponseEntity<ApiResponse<Page<PaymentResponse>>> getChildPayments(
            @RequestParam Long studentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        AuthenticatedUser authUser = getAuthenticatedParent();
        Long resolvedSchoolId = validateParentAccessToChildAndGetSchoolId(authUser.userId(), studentId);

        Page<PaymentResponse> payments = feePaymentService.getPaymentsByStudent(
                studentId, resolvedSchoolId, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        );
        return ResponseEntity.ok(ApiResponse.success(payments, "Payment history fetched successfully"));
    }

    private AuthenticatedUser getAuthenticatedParent() {
        AuthenticatedUser authUser = AuthContextHolder.get();
        if (authUser == null) {
            throw new UnauthorizedException("Not authenticated");
        }
        return authUser;
    }

    private Long validateParentAccessToChildAndGetSchoolId(Long parentUserId, Long studentId) {
        return studentParentRepository.findByIdParentUserId(parentUserId)
                .stream()
                .filter(sp -> sp.getStudent().getId().equals(studentId))
                .findFirst()
                .map(sp -> sp.getStudent().getSchool().getId())
                .orElseThrow(() -> new UnauthorizedException("You do not have access to this student's records. (Parent ID: " + parentUserId + ", Requested Student ID: " + studentId + ")"));
    }
}
