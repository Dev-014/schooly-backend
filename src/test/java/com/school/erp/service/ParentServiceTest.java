package com.school.erp.service;

import com.school.erp.dto.parent.ParentChildResponse;
import com.school.erp.entity.School;
import com.school.erp.entity.SchoolClass;
import com.school.erp.entity.Student;
import com.school.erp.entity.StudentParent;
import com.school.erp.entity.User;
import com.school.erp.repository.StudentParentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ParentServiceTest {

    @Mock
    private StudentParentRepository studentParentRepository;

    private ParentService parentService;

    @BeforeEach
    void setUp() {
        parentService = new ParentService(studentParentRepository);
    }

    @Test
    void getChildren_shouldReturnChildrenListSuccessfully() {
        User parent = new User();
        parent.setId(7L);

        School school = new School();
        school.setId(10L);
        school.setName("Greenwood High");

        SchoolClass schoolClass = new SchoolClass();
        schoolClass.setId(3L);
        schoolClass.setName("Grade 10");

        Student student = new Student();
        student.setId(21L);
        student.setName("Alex");
        student.setAdmissionNo("ADM-21");
        student.setSchool(school);
        student.setSchoolClass(schoolClass);

        StudentParent sp = new StudentParent();
        sp.setParentUser(parent);
        sp.setStudent(student);

        when(studentParentRepository.findByIdParentUserIdAndStudentSchoolId(7L, 10L))
                .thenReturn(List.of(sp));

        List<ParentChildResponse> result = parentService.getChildren(7L, 10L);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(21L, result.get(0).studentId());
        assertEquals("Alex", result.get(0).name());
        assertEquals("ADM-21", result.get(0).admissionNo());
        assertEquals(10L, result.get(0).schoolId());
        assertEquals(3L, result.get(0).classId());
    }
}
