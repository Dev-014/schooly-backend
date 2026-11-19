package com.school.erp.service.finance.impl;

import com.school.erp.dto.payment.FeeDueResponse;
import com.school.erp.entity.finance.CollectionPlanItem;
import com.school.erp.entity.finance.FeeCategory;
import com.school.erp.entity.finance.FeeDue;
import com.school.erp.entity.finance.FeeStructure;
import com.school.erp.entity.finance.FeeStructureItem;
import com.school.erp.entity.superadmin.School;
import com.school.erp.repository.finance.FeeCategoryRepository;
import com.school.erp.repository.finance.FeeDueRepository;
import com.school.erp.repository.finance.FeeGenerationBatchRepository;
import com.school.erp.repository.finance.FeeStructureRepository;
import com.school.erp.repository.finance.StudentFeeStructureRepository;
import com.school.erp.repository.student.StudentRepository;
import com.school.erp.repository.superadmin.SchoolRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FeeDueServiceImplTest {

    @Mock
    private FeeDueRepository feeDueRepository;
    @Mock
    private FeeCategoryRepository feeCategoryRepository;
    @Mock
    private SchoolRepository schoolRepository;
    @Mock
    private StudentRepository studentRepository;
    @Mock
    private FeeStructureRepository feeStructureRepository;
    @Mock
    private StudentFeeStructureRepository studentFeeStructureRepository;
    @Mock
    private FeeGenerationBatchRepository feeGenerationBatchRepository;

    private FeeDueServiceImpl feeDueService;

    @BeforeEach
    void setUp() {
        feeDueService = new FeeDueServiceImpl(
                feeDueRepository,
                feeCategoryRepository,
                schoolRepository,
                studentRepository,
                feeStructureRepository,
                studentFeeStructureRepository,
                feeGenerationBatchRepository
        );
    }

    @Test
    void getStudentDues_resolvesDistinctTermAndCategoryForCollectionPlanInstallmentsAndAnnualItems() {
        School school = new School();
        school.setId(4L);

        FeeCategory tuitionCategory = new FeeCategory();
        tuitionCategory.setId(15L);
        tuitionCategory.setName("Tuition Fee");

        FeeCategory sportsCategory = new FeeCategory();
        sportsCategory.setId(27L);
        sportsCategory.setName("Sports Fee");

        FeeStructureItem tuitionItem = new FeeStructureItem();
        tuitionItem.setFeeCategory(tuitionCategory);
        tuitionItem.setAmount(new BigDecimal("45000.00"));
        tuitionItem.setIsPartOfCollectionPlan(true);

        FeeStructure structure = new FeeStructure();
        structure.setId(42L);
        structure.setItems(List.of(tuitionItem));

        CollectionPlanItem planItem1 = new CollectionPlanItem();
        planItem1.setId(1L);
        planItem1.setLabel("Term 1");

        FeeDue planInstallmentDue = new FeeDue();
        planInstallmentDue.setId(501L);
        planInstallmentDue.setSchool(school);
        planInstallmentDue.setTitle("Term 1");
        planInstallmentDue.setTermName("Term 1");
        planInstallmentDue.setFeeStructure(structure);
        planInstallmentDue.setCollectionPlanItem(planItem1);
        planInstallmentDue.setAmount(new BigDecimal("18000.00"));
        planInstallmentDue.setPaidAmount(BigDecimal.ZERO);
        planInstallmentDue.setDiscountAmount(BigDecimal.ZERO);
        planInstallmentDue.setDueDate(LocalDate.of(2026, 4, 1));
        planInstallmentDue.setStatus("PENDING");

        FeeDue nonPlanAnnualDue = new FeeDue();
        nonPlanAnnualDue.setId(502L);
        nonPlanAnnualDue.setSchool(school);
        nonPlanAnnualDue.setTitle("Sports Fee");
        nonPlanAnnualDue.setTermName("Sports Fee"); // legacy duplicate value should normalize to Annual
        nonPlanAnnualDue.setFeeCategory(sportsCategory);
        nonPlanAnnualDue.setAmount(new BigDecimal("2500.00"));
        nonPlanAnnualDue.setPaidAmount(BigDecimal.ZERO);
        nonPlanAnnualDue.setDiscountAmount(BigDecimal.ZERO);
        nonPlanAnnualDue.setDueDate(LocalDate.of(2026, 6, 1));
        nonPlanAnnualDue.setStatus("PENDING");

        when(feeDueRepository.findByStudentIdAndSchoolIdOrderByDueDateAsc(101L, 4L))
                .thenReturn(List.of(planInstallmentDue, nonPlanAnnualDue));

        List<FeeDueResponse> responses = feeDueService.getStudentDues(101L, 4L, null);

        assertEquals(2, responses.size());
        assertEquals("Term 1", responses.get(0).termName());
        assertEquals("Tuition Fee", responses.get(0).feeCategoryName());
        assertEquals(15L, responses.get(0).feeCategoryId());

        assertEquals("Annual", responses.get(1).termName());
        assertEquals("Sports Fee", responses.get(1).feeCategoryName());
        assertEquals(27L, responses.get(1).feeCategoryId());
    }

    @Test
    void getStudentDues_resolvesAdditionalFeeForAdHocItems() {
        School school = new School();
        school.setId(4L);

        FeeCategory uniformCategory = new FeeCategory();
        uniformCategory.setId(33L);
        uniformCategory.setName("Uniform & Books");

        FeeDue adHocDue = new FeeDue();
        adHocDue.setId(701L);
        adHocDue.setSchool(school);
        adHocDue.setTitle("Uniform & Books (Additional Fee)");
        adHocDue.setTermName("Ad-Hoc");
        adHocDue.setIsAdHoc(true);
        adHocDue.setFeeCategory(uniformCategory);
        adHocDue.setAmount(new BigDecimal("3500.00"));
        adHocDue.setPaidAmount(BigDecimal.ZERO);
        adHocDue.setDiscountAmount(BigDecimal.ZERO);
        adHocDue.setDueDate(LocalDate.of(2026, 7, 15));
        adHocDue.setStatus("PENDING");

        when(feeDueRepository.findByStudentIdAndSchoolIdOrderByDueDateAsc(101L, 4L))
                .thenReturn(List.of(adHocDue));

        List<FeeDueResponse> responses = feeDueService.getStudentDues(101L, 4L, null);

        assertEquals(1, responses.size());
        assertEquals("Additional Fee", responses.get(0).termName());
        assertEquals("Uniform & Books", responses.get(0).feeCategoryName());
        assertEquals(33L, responses.get(0).feeCategoryId());
    }
}
