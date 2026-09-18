package com.school.erp.service.impl;

import com.school.erp.dto.payment.FeeDueResponse;
import com.school.erp.dto.payment.FeeGenerationPreviewRequest;
import com.school.erp.dto.payment.FeeGenerationPreviewResponse;
import com.school.erp.dto.payment.FeeGenerationConfirmRequest;
import com.school.erp.repository.FeeDueRepository;
import com.school.erp.repository.FeeCategoryRepository;
import com.school.erp.repository.SchoolRepository;
import com.school.erp.repository.StudentRepository;
import com.school.erp.repository.FeeGenerationBatchRepository;
import com.school.erp.service.FeeDueService;
import com.school.erp.entity.FeeDue;
import com.school.erp.entity.FeeCategory;
import com.school.erp.entity.School;
import com.school.erp.entity.Student;
import com.school.erp.entity.FeeStructure;
import com.school.erp.entity.FeeStructureItem;
import com.school.erp.entity.CollectionPlan;
import com.school.erp.entity.CollectionPlanItem;
import com.school.erp.entity.StudentFeeStructure;
import com.school.erp.entity.FeeGenerationBatch;
import com.school.erp.repository.FeeStructureRepository;
import com.school.erp.repository.StudentFeeStructureRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class FeeDueServiceImpl implements FeeDueService {

    private final FeeDueRepository feeDueRepository;
    private final FeeCategoryRepository feeCategoryRepository;
    private final SchoolRepository schoolRepository;
    private final StudentRepository studentRepository;
    private final FeeStructureRepository feeStructureRepository;
    private final StudentFeeStructureRepository studentFeeStructureRepository;
    private final FeeGenerationBatchRepository feeGenerationBatchRepository;

    public FeeDueServiceImpl(FeeDueRepository feeDueRepository, FeeCategoryRepository feeCategoryRepository, SchoolRepository schoolRepository, StudentRepository studentRepository, FeeStructureRepository feeStructureRepository, StudentFeeStructureRepository studentFeeStructureRepository, FeeGenerationBatchRepository feeGenerationBatchRepository) {
        this.feeDueRepository = feeDueRepository;
        this.feeCategoryRepository = feeCategoryRepository;
        this.schoolRepository = schoolRepository;
        this.studentRepository = studentRepository;
        this.feeStructureRepository = feeStructureRepository;
        this.studentFeeStructureRepository = studentFeeStructureRepository;
        this.feeGenerationBatchRepository = feeGenerationBatchRepository;
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

        Optional<StudentFeeStructure> sfsOpt = studentFeeStructureRepository.findFirstByStudentIdAndAcademicYearId(studentId, student.getAcademicYearId());
        
        FeeStructure activeStructure = null;
        CollectionPlan collectionPlan = null;
        
        if (sfsOpt.isPresent()) {
            activeStructure = sfsOpt.get().getFeeStructure();
            collectionPlan = sfsOpt.get().getCollectionPlan();
        } else {
            List<FeeStructure> structures = feeStructureRepository.findBySchoolIdAndAcademicYearId(schoolId, student.getAcademicYearId());
            activeStructure = structures.stream()
                    .filter(fs -> Boolean.TRUE.equals(fs.getIsActive()) && fs.getSchoolClass() != null && fs.getSchoolClass().getId().equals(student.getSchoolClass().getId()))
                    .findFirst()
                    .orElse(null);
        }

        if (activeStructure == null) {
            System.out.println("No active fee structure found for student's class.");
            return java.util.Collections.emptyList();
        }
        
        final FeeStructure finalActiveStructure = activeStructure;

        List<FeeDue> existingDues = feeDueRepository.findByStudentIdAndSchoolIdOrderByDueDateAsc(studentId, schoolId);
        boolean hasBaseDues = existingDues.stream()
                .anyMatch(d -> Boolean.FALSE.equals(d.getIsAdHoc()) && finalActiveStructure.equals(d.getFeeStructure()));
        
        if (hasBaseDues) {
            return getStudentDues(studentId, schoolId, null);
        }

        if (collectionPlan == null && activeStructure.getCollectionPlan() != null) {
            collectionPlan = activeStructure.getCollectionPlan();
        }

        List<FeeDue> newDues = createDuesForStudent(student, activeStructure, collectionPlan);
        feeDueRepository.saveAll(newDues);
        return getStudentDues(studentId, schoolId, null);
    }

    private List<FeeDue> createDuesForStudent(Student student, FeeStructure activeStructure, CollectionPlan collectionPlan) {
        List<FeeDue> newDues = new ArrayList<>();

        if (collectionPlan != null && collectionPlan.getItems() != null && !collectionPlan.getItems().isEmpty()) {
            BigDecimal totalPlanAmount = BigDecimal.ZERO;
            List<FeeStructureItem> excludedItems = new ArrayList<>();
            
            for (FeeStructureItem item : activeStructure.getItems()) {
                if (Boolean.TRUE.equals(item.getIsPartOfCollectionPlan())) {
                    totalPlanAmount = totalPlanAmount.add(item.getAmount());
                } else {
                    excludedItems.add(item);
                }
            }
            
            for (FeeStructureItem exItem : excludedItems) {
                FeeDue due = new FeeDue();
                due.setStudent(student);
                due.setSchool(student.getSchool());
                due.setFeeCategory(exItem.getFeeCategory());
                due.setFeeStructure(activeStructure);
                due.setIsAdHoc(false);
                due.setTitle(exItem.getFeeCategory().getName());
                due.setAmount(exItem.getAmount());
                due.setTermName(exItem.getFeeCategory().getName());
                due.setDueDate(LocalDate.now().plusDays(30));
                due.setStatus("UNPAID");
                newDues.add(due);
            }
            
            BigDecimal totalDistributed = BigDecimal.ZERO;
            List<CollectionPlanItem> planItems = collectionPlan.getItems();
            planItems.sort((a, b) -> a.getSequenceOrder().compareTo(b.getSequenceOrder()));
            
            for (int i = 0; i < planItems.size(); i++) {
                CollectionPlanItem cpItem = planItems.get(i);
                BigDecimal amountForThisInst = BigDecimal.ZERO;
                
                if ("PERCENTAGE".equalsIgnoreCase(cpItem.getAmountType())) {
                    amountForThisInst = totalPlanAmount.multiply(cpItem.getAmountValue()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                } else if ("FIXED".equalsIgnoreCase(cpItem.getAmountType())) {
                    amountForThisInst = cpItem.getAmountValue();
                } else if ("REMAINDER".equalsIgnoreCase(cpItem.getAmountType()) || i == planItems.size() - 1) {
                    amountForThisInst = totalPlanAmount.subtract(totalDistributed);
                }
                
                if (i == planItems.size() - 1 && "PERCENTAGE".equalsIgnoreCase(cpItem.getAmountType())) {
                    amountForThisInst = totalPlanAmount.subtract(totalDistributed);
                }
                
                FeeDue due = new FeeDue();
                due.setStudent(student);
                due.setSchool(student.getSchool());
                due.setFeeStructure(activeStructure);
                due.setCollectionPlanItem(cpItem);
                due.setIsAdHoc(false);
                due.setTitle(cpItem.getLabel());
                due.setAmount(amountForThisInst);
                due.setTermName(cpItem.getLabel());
                due.setDueDate(cpItem.getDueDate() != null ? cpItem.getDueDate() : LocalDate.now().plusDays(30));
                due.setStatus("UNPAID");
                
                newDues.add(due);
                totalDistributed = totalDistributed.add(amountForThisInst);
            }
            
        } else {
            for (FeeStructureItem item : activeStructure.getItems()) {
                FeeDue due = new FeeDue();
                due.setStudent(student);
                due.setSchool(student.getSchool());
                due.setFeeCategory(item.getFeeCategory());
                due.setFeeStructure(activeStructure);
                due.setIsAdHoc(false);
                due.setTitle(item.getFeeCategory().getName());
                due.setAmount(item.getAmount());
                due.setTermName(item.getFeeCategory().getName());
                due.setDueDate(LocalDate.now().plusDays(30));
                due.setStatus("UNPAID");
                newDues.add(due);
            }
        }
        return newDues;
    }

    @Override
    public FeeGenerationPreviewResponse previewBatchGeneration(Long schoolId, FeeGenerationPreviewRequest request) {
        List<Student> classStudents = studentRepository.findBySchoolIdAndSchoolClassId(schoolId, request.classId());
        
        FeeStructure feeStructure = feeStructureRepository.findById(request.feeStructureId())
                .orElseThrow(() -> new RuntimeException("Fee structure not found"));

        if (!feeStructure.getSchoolClass().getId().equals(request.classId()) || !feeStructure.getAcademicYearId().equals(request.academicYearId())) {
            throw new RuntimeException("Fee structure does not match the given class and academic year");
        }

        List<Student> eligibleStudents = classStudents.stream()
                .filter(s -> request.academicYearId().equals(s.getAcademicYearId()))
                .filter(s -> "ACTIVE".equalsIgnoreCase(s.getStatus()))
                .filter(s -> !feeDueRepository.existsByStudentIdAndFeeStructureIdAndIsAdHocFalse(s.getId(), request.feeStructureId()))
                .collect(Collectors.toList());

        BigDecimal perStudentTotal = BigDecimal.ZERO;
        for (FeeStructureItem item : feeStructure.getItems()) {
            perStudentTotal = perStudentTotal.add(item.getAmount() != null ? item.getAmount() : BigDecimal.ZERO);
        }

        final BigDecimal finalPerStudentTotal = perStudentTotal;
        BigDecimal totalAmount = finalPerStudentTotal.multiply(BigDecimal.valueOf(eligibleStudents.size()));

        List<FeeGenerationPreviewResponse.FeeGenerationPreviewStudent> previewList = eligibleStudents.stream()
                .map(s -> new FeeGenerationPreviewResponse.FeeGenerationPreviewStudent(
                        s.getId(),
                        s.getAdmissionNo(),
                        (s.getFirstName() != null ? s.getFirstName() : "") + " " + (s.getLastName() != null ? s.getLastName() : ""),
                        finalPerStudentTotal
                ))
                .collect(Collectors.toList());

        int totalInClass = (int) classStudents.stream().filter(s -> request.academicYearId().equals(s.getAcademicYearId())).count();

        return new FeeGenerationPreviewResponse(
                eligibleStudents.size(),
                totalInClass,
                totalAmount,
                previewList
        );
    }

    @Override
    @Transactional
    public void confirmBatchGeneration(Long schoolId, FeeGenerationConfirmRequest request, String generatedBy) {
        List<Student> classStudents = studentRepository.findBySchoolIdAndSchoolClassId(schoolId, request.classId());
        
        FeeStructure feeStructure = feeStructureRepository.findById(request.feeStructureId())
                .orElseThrow(() -> new RuntimeException("Fee structure not found"));

        if (!feeStructure.getSchoolClass().getId().equals(request.classId()) || !feeStructure.getAcademicYearId().equals(request.academicYearId())) {
            throw new RuntimeException("Fee structure does not match the given class and academic year");
        }

        List<Student> eligibleStudents = classStudents.stream()
                .filter(s -> request.academicYearId().equals(s.getAcademicYearId()))
                .filter(s -> "ACTIVE".equalsIgnoreCase(s.getStatus()))
                .filter(s -> !feeDueRepository.existsByStudentIdAndFeeStructureIdAndIsAdHocFalse(s.getId(), request.feeStructureId()))
                .collect(Collectors.toList());

        if (eligibleStudents.isEmpty()) {
            throw new RuntimeException("No eligible students found for fee generation");
        }

        CollectionPlan collectionPlan = feeStructure.getCollectionPlan();
        
        List<FeeDue> allNewDues = new ArrayList<>();
        BigDecimal perStudentTotal = BigDecimal.ZERO;
        for (FeeStructureItem item : feeStructure.getItems()) {
            perStudentTotal = perStudentTotal.add(item.getAmount() != null ? item.getAmount() : BigDecimal.ZERO);
        }
        
        BigDecimal totalAmountGenerated = perStudentTotal.multiply(BigDecimal.valueOf(eligibleStudents.size()));

        for (Student student : eligibleStudents) {
            allNewDues.addAll(createDuesForStudent(student, feeStructure, collectionPlan));
        }

        feeDueRepository.saveAll(allNewDues);

        FeeGenerationBatch batch = new FeeGenerationBatch();
        batch.setSchool(feeStructure.getSchool());
        batch.setAcademicYearId(request.academicYearId());
        batch.setSchoolClass(feeStructure.getSchoolClass());
        batch.setFeeStructure(feeStructure);
        batch.setCollectionPlan(collectionPlan);
        batch.setNumberOfStudents(eligibleStudents.size());
        batch.setTotalAmountGenerated(totalAmountGenerated);
        batch.setGeneratedBy(generatedBy);
        batch.setGeneratedAt(LocalDateTime.now());

        feeGenerationBatchRepository.save(batch);
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
