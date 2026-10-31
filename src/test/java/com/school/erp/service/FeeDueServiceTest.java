package com.school.erp.service;

import com.school.erp.entity.finance.FeeCategory;
import com.school.erp.entity.finance.FeeDue;
import com.school.erp.entity.superadmin.School;
import com.school.erp.entity.student.Student;
import com.school.erp.repository.finance.FeeCategoryRepository;
import com.school.erp.repository.finance.FeeDueRepository;
import com.school.erp.repository.superadmin.SchoolRepository;
import com.school.erp.repository.student.StudentRepository;
import com.school.erp.dto.payment.FeeDueResponse;
import com.school.erp.service.finance.FeeDueService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@SpringBootTest
@ActiveProfiles("test")
public class FeeDueServiceTest {

    @Autowired
    private FeeDueService feeDueService;
    
    @Autowired
    private FeeDueRepository feeDueRepository;
    
    @Autowired
    private StudentRepository studentRepository;
    
    @Autowired
    private SchoolRepository schoolRepository;
    
    @Autowired
    private FeeCategoryRepository feeCategoryRepository;

    @Test
    @Transactional
    public void testGetDues() {
        School school = schoolRepository.findAll().get(0);
        Student student = studentRepository.findAll().get(0);
        
        FeeCategory cat = new FeeCategory();
        cat.setName("Test");
        cat.setSchool(school);
        cat = feeCategoryRepository.save(cat);
        
        FeeDue due = new FeeDue();
        due.setSchool(school);
        due.setStudent(student);
        due.setFeeCategory(cat);
        due.setTitle("Test Due");
        due.setAmount(BigDecimal.TEN);
        due.setDueDate(LocalDate.now());
        feeDueRepository.save(due);
        
        List<FeeDueResponse> dues = feeDueService.getStudentDues(student.getId(), school.getId(), null);
        org.junit.jupiter.api.Assertions.assertFalse(dues.isEmpty());
        org.junit.jupiter.api.Assertions.assertNotNull(dues.get(0).feeCategoryId());
        org.junit.jupiter.api.Assertions.assertNotNull(dues.get(0).feeCategoryName());
        org.junit.jupiter.api.Assertions.assertNotEquals("Unknown", dues.get(0).feeCategoryName());
    }

    @Test
    @Transactional
    public void testDueCategoryFallbackWhenFeeCategoryNull() {
        School school = schoolRepository.findAll().get(0);
        Student student = studentRepository.findAll().get(0);

        FeeDue due = new FeeDue();
        due.setSchool(school);
        due.setStudent(student);
        due.setFeeCategory(null); // Explicitly null
        due.setTitle("Term 1");
        due.setAmount(new BigDecimal("10400.00"));
        due.setDueDate(LocalDate.now());
        feeDueRepository.save(due);

        List<FeeDueResponse> dues = feeDueService.getStudentDues(student.getId(), school.getId(), null);
        org.junit.jupiter.api.Assertions.assertFalse(dues.isEmpty());
        FeeDueResponse termDue = dues.stream().filter(d -> "Term 1".equals(d.title())).findFirst().orElseThrow();
        org.junit.jupiter.api.Assertions.assertNotNull(termDue.feeCategoryName());
        org.junit.jupiter.api.Assertions.assertNotEquals("Unknown", termDue.feeCategoryName());
    }
}
