package com.school.erp.service;

import com.school.erp.entity.FeeCategory;
import com.school.erp.entity.FeeDue;
import com.school.erp.entity.School;
import com.school.erp.entity.Student;
import com.school.erp.repository.FeeCategoryRepository;
import com.school.erp.repository.FeeDueRepository;
import com.school.erp.repository.SchoolRepository;
import com.school.erp.repository.StudentRepository;
import com.school.erp.dto.payment.FeeDueResponse;
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
        System.out.println("Dues size: " + dues.size());
        System.out.println("Category Name: " + dues.get(0).feeCategoryName());
    }
}
