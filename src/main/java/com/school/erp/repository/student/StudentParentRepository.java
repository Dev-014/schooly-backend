package com.school.erp.repository.student;

import com.school.erp.entity.student.StudentParent;
import com.school.erp.entity.student.StudentParentId;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface StudentParentRepository extends JpaRepository<StudentParent, StudentParentId> {

    @EntityGraph(attributePaths = {"student", "student.school", "student.schoolClass"})
    List<StudentParent> findByIdParentUserIdAndStudentSchoolId(Long parentUserId, Long schoolId);

    @EntityGraph(attributePaths = {"student", "student.school", "student.schoolClass"})
    List<StudentParent> findByIdParentUserId(Long parentUserId);

    @EntityGraph(attributePaths = {"parentUser", "student"})
    List<StudentParent> findByIdStudentId(Long studentId);

    @EntityGraph(attributePaths = {"parentUser", "student"})
    List<StudentParent> findByIdStudentIdIn(Collection<Long> studentIds);

    Optional<StudentParent> findByIdParentUserIdAndStudentId(Long parentUserId, Long studentId);
}
