package com.school.erp.repository;

import com.school.erp.entity.StudentParent;
import com.school.erp.entity.StudentParentId;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StudentParentRepository extends JpaRepository<StudentParent, StudentParentId> {

    @EntityGraph(attributePaths = {"student", "student.school", "student.schoolClass"})
    List<StudentParent> findByIdParentUserIdAndStudentSchoolId(Long parentUserId, Long schoolId);

    @EntityGraph(attributePaths = {"student", "student.school", "student.schoolClass"})
    List<StudentParent> findByIdParentUserId(Long parentUserId);
}
