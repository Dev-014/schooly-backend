package com.school.erp.service.impl;

import com.school.erp.dto.payment.FeeDueResponse;
import com.school.erp.repository.FeeDueRepository;
import com.school.erp.repository.FeeCategoryRepository;
import com.school.erp.repository.SchoolRepository;
import com.school.erp.repository.StudentRepository;
import com.school.erp.service.FeeDueService;
import com.school.erp.entity.FeeDue;
import com.school.erp.entity.FeeCategory;
import com.school.erp.entity.School;
import com.school.erp.entity.Student;
import com.school.erp.entity.FeeStructure;
import com.school.erp.entity.FeeStructureItem;
import com.school.erp.entity.FeeInstallment;
import com.school.erp.repository.FeeStructureRepository;
import com.school.erp.repository.FeeInstallmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class FeeDueServiceImpl implements FeeDueService {

    private final FeeDueRepository feeDueRepository;
    private final FeeCategoryRepository feeCategoryRepository;
    private final SchoolRepository schoolRepository;
    private final StudentRepository studentRepository;
    private final FeeStructureRepository feeStructureRepository;
    private final FeeInstallmentRepository feeInstallmentRepository;

    public FeeDueServiceImpl(FeeDueRepository feeDueRepository, FeeCategoryRepository feeCategoryRepository, SchoolRepository schoolRepository, StudentRepository studentRepository, FeeStructureRepository feeStructureRepository, FeeInstallmentRepository feeInstallmentRepository) {
        this.feeDueRepository = feeDueRepository;
        this.feeCategoryRepository = feeCategoryRepository;
        this.schoolRepository = schoolRepository;
        this.studentRepository = studentRepository;
        this.feeStructureRepository = feeStructureRepository;
        this.feeInstallmentRepository = feeInstallmentRepository;
    }

    @Override
    public List<FeeDueResponse> getStudentDues(Long studentId, Long schoolId, List<String> statuses) {
        List<com.school.erp.entity.FeeDue> dues;
        try {
            if (statuses == null || statuses.isEmpty()) {
                dues = feeDueRepository.findByStudentIdAndSchoolIdOrderByDueDateAsc(studentId, schoolId);
            } else {
                dues = feeDueRepository.findByStudentIdAndSchoolIdAndStatusInOrderByDueDateAsc(studentId, schoolId, statuses);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Database error fetching dues: " + e.getMessage(), e);
        }

        return dues.stream().map(due -> {
            try {
                return new FeeDueResponse(
                        due.getId(),
                        due.getTitle(),
                        due.getAmount(),
                        due.getPaidAmount() != null ? due.getPaidAmount() : java.math.BigDecimal.ZERO,
                        due.getDiscountAmount() != null ? due.getDiscountAmount() : java.math.BigDecimal.ZERO,
                        due.getDueDate(),
                        due.getStatus(),
                        due.getFeeCategory() != null ? due.getFeeCategory().getId() : null,
                        due.getFeeCategory() != null ? due.getFeeCategory().getName() : "Unknown",
                        due.getTermName()
                );
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }).filter(java.util.Objects::nonNull).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public FeeDueResponse applyDiscount(Long studentId, Long dueId, Long schoolId, com.school.erp.dto.payment.ApplyDiscountRequest request) {
        com.school.erp.entity.FeeDue due = feeDueRepository.findById(dueId)
                .orElseThrow(() -> new RuntimeException("Fee due not found"));

        if (!due.getStudent().getId().equals(studentId) || !due.getSchool().getId().equals(schoolId)) {
            throw new RuntimeException("Fee due does not belong to the student/school");
        }

        if (due.getPaidAmount().add(request.discountAmount()).compareTo(due.getAmount()) > 0) {
            throw new RuntimeException("Discount exceeds the remaining amount due");
        }

        due.setDiscountAmount(request.discountAmount());
        
        // Update status if fully covered by paid + discount
        if (due.getPaidAmount().add(due.getDiscountAmount()).compareTo(due.getAmount()) >= 0) {
            due.setStatus("PAID");
        }

        feeDueRepository.save(due);

        return new FeeDueResponse(
                due.getId(),
                due.getTitle(),
                due.getAmount(),
                due.getPaidAmount() != null ? due.getPaidAmount() : java.math.BigDecimal.ZERO,
                due.getDiscountAmount() != null ? due.getDiscountAmount() : java.math.BigDecimal.ZERO,
                due.getDueDate(),
                due.getStatus(),
                due.getFeeCategory() != null ? due.getFeeCategory().getId() : null,
                due.getFeeCategory() != null ? due.getFeeCategory().getName() : "Unknown",
                due.getTermName()
        );
    }

    @Override
    @Transactional
    public void generateDuesFromStructure(Long studentId, Long feeStructureId, Long academicYearId, Long schoolId) {
        // Implementation stub
    }

    @Override
    @Transactional
    public List<FeeDueResponse> generateBaseDuesForStudent(Long studentId, Long schoolId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        
        if (student.getSchoolClass() == null) {
            throw new RuntimeException("Student is not assigned to a class.");
        }
        
        if (student.getAcademicYearId() == null) {
            throw new RuntimeException("Student is not assigned to an academic year.");
        }

        // Find active fee structure for this class
        List<FeeStructure> structures = feeStructureRepository.findBySchoolIdAndAcademicYearId(schoolId, student.getAcademicYearId());
        FeeStructure activeStructure = structures.stream()
                .filter(fs -> Boolean.TRUE.equals(fs.getIsActive()) && fs.getSchoolClass() != null && fs.getSchoolClass().getId().equals(student.getSchoolClass().getId()))
                .findFirst()
                .orElse(null);

        if (activeStructure == null) {
            throw new RuntimeException("No active fee structure found for student's class.");
        }

        // Find existing non-ad-hoc dues to prevent duplicates
        List<FeeDue> existingDues = feeDueRepository.findByStudentIdAndSchoolIdOrderByDueDateAsc(studentId, schoolId);
        boolean hasBaseDues = existingDues.stream()
                .anyMatch(d -> Boolean.FALSE.equals(d.getIsAdHoc()) && activeStructure.equals(d.getFeeStructure()));
        
        if (hasBaseDues) {
            // Already generated, just return them
            return getStudentDues(studentId, schoolId, null);
        }

        // Find installments for the academic year
        List<FeeInstallment> installments = feeInstallmentRepository.findBySchoolIdAndAcademicYearId(schoolId, student.getAcademicYearId());
        if (installments.isEmpty()) {
            throw new RuntimeException("No fee installments defined for the academic year. Cannot split dues.");
        }

        List<FeeDue> newDues = new ArrayList<>();
        BigDecimal numInstallments = BigDecimal.valueOf(installments.size());

        for (FeeStructureItem item : activeStructure.getItems()) {
            BigDecimal splitAmount = item.getAmount().divide(numInstallments, 2, RoundingMode.HALF_UP);
            
            // Adjust last installment to account for rounding differences
            BigDecimal totalDistributed = BigDecimal.ZERO;
            
            for (int i = 0; i < installments.size(); i++) {
                FeeInstallment inst = installments.get(i);
                
                BigDecimal amountForThisInst = splitAmount;
                if (i == installments.size() - 1) {
                    amountForThisInst = item.getAmount().subtract(totalDistributed);
                }
                
                FeeDue due = new FeeDue();
                due.setStudent(student);
                due.setSchool(student.getSchool());
                due.setFeeCategory(item.getFeeCategory());
                due.setFeeStructure(activeStructure);
                due.setFeeInstallment(inst);
                due.setIsAdHoc(false);
                due.setTitle(item.getFeeCategory().getName());
                due.setAmount(amountForThisInst);
                due.setTermName(inst.getName());
                due.setDueDate(inst.getDueDate() != null ? inst.getDueDate() : LocalDate.now().plusDays(30));
                due.setStatus("UNPAID");
                
                newDues.add(due);
                totalDistributed = totalDistributed.add(amountForThisInst);
            }
        }
        
        feeDueRepository.saveAll(newDues);
        return getStudentDues(studentId, schoolId, null);
    }

    @Override
    @Transactional
    public FeeDueResponse assignAdHocFee(Long studentId, Long schoolId, Long feeCategoryId, BigDecimal amount, String title, LocalDate dueDate) {
        Student student = studentRepository.findById(studentId).orElseThrow(() -> new RuntimeException("Student not found"));
        School school = schoolRepository.findById(schoolId).orElseThrow(() -> new RuntimeException("School not found"));
        FeeCategory category = feeCategoryRepository.findById(feeCategoryId).orElseThrow(() -> new RuntimeException("Category not found"));

        FeeDue due = new FeeDue();
        due.setStudent(student);
        due.setSchool(school);
        due.setFeeCategory(category);
        due.setAmount(amount);
        due.setTitle(title);
        due.setDueDate(dueDate);
        due.setStatus("UNPAID");
        due.setIsAdHoc(true);
        due.setTermName("Ad-Hoc");

        due = feeDueRepository.save(due);
        return new FeeDueResponse(
                due.getId(),
                due.getTitle(),
                due.getAmount(),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                due.getDueDate(),
                due.getStatus(),
                category.getId(),
                category.getName(),
                due.getTermName()
        );
    }
}
