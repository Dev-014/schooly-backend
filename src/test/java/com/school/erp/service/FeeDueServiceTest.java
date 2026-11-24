package com.school.erp.service;

import com.school.erp.dto.payment.FeeDueResponse;
import com.school.erp.entity.finance.FeeCategory;
import com.school.erp.entity.finance.FeeDue;
import com.school.erp.entity.student.Student;
import com.school.erp.entity.superadmin.School;
import com.school.erp.repository.finance.FeeCategoryRepository;
import com.school.erp.repository.finance.FeeDueRepository;
import com.school.erp.repository.finance.FeeGenerationBatchRepository;
import com.school.erp.repository.finance.FeeStructureRepository;
import com.school.erp.repository.finance.StudentFeeStructureRepository;
import com.school.erp.repository.student.StudentRepository;
import com.school.erp.repository.superadmin.SchoolRepository;
import com.school.erp.service.finance.impl.FeeDueServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class FeeDueServiceTest {

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

    @InjectMocks
    private FeeDueServiceImpl feeDueService;

    @Test
    public void testGetDues() {
        School school = new School();
        school.setId(1L);

        Student student = new Student();
        student.setId(10L);

        FeeCategory cat = new FeeCategory();
        cat.setId(100L);
        cat.setName("Test");
        cat.setSchool(school);

        FeeDue due = new FeeDue();
        due.setId(1000L);
        due.setSchool(school);
        due.setStudent(student);
        due.setFeeCategory(cat);
        due.setTitle("Test Due");
        due.setAmount(BigDecimal.TEN);
        due.setDueDate(LocalDate.now());

        when(feeDueRepository.findByStudentIdAndSchoolIdOrderByDueDateAsc(student.getId(), school.getId()))
                .thenReturn(List.of(due));

        List<FeeDueResponse> dues = feeDueService.getStudentDues(student.getId(), school.getId(), null);
        assertFalse(dues.isEmpty());
        assertNotNull(dues.get(0).feeCategoryId());
        assertNotNull(dues.get(0).feeCategoryName());
        assertNotEquals("Unknown", dues.get(0).feeCategoryName());
    }

    @Test
    public void testDueCategoryFallbackWhenFeeCategoryNull() {
        School school = new School();
        school.setId(1L);

        Student student = new Student();
        student.setId(10L);

        FeeDue due = new FeeDue();
        due.setId(1001L);
        due.setSchool(school);
        due.setStudent(student);
        due.setFeeCategory(null); // Explicitly null
        due.setTitle("Term 1");
        due.setAmount(new BigDecimal("10400.00"));
        due.setDueDate(LocalDate.now());

        when(feeDueRepository.findByStudentIdAndSchoolIdOrderByDueDateAsc(student.getId(), school.getId()))
                .thenReturn(List.of(due));

        List<FeeDueResponse> dues = feeDueService.getStudentDues(student.getId(), school.getId(), null);
        assertFalse(dues.isEmpty());
        FeeDueResponse termDue = dues.stream().filter(d -> "Term 1".equals(d.title())).findFirst().orElseThrow();
        assertNotNull(termDue.feeCategoryName());
        assertNotEquals("Unknown", termDue.feeCategoryName());
    }
}
