package com.school.erp.repository;

import com.school.erp.entity.StudentSibling;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentSiblingRepository extends JpaRepository<StudentSibling, Long> {
    List<StudentSibling> findByPrimaryStudentIdAndSchoolId(Long primaryStudentId, Long schoolId);
    Optional<StudentSibling> findByPrimaryStudentIdAndSiblingStudentIdAndSchoolId(Long primaryStudentId, Long siblingStudentId, Long schoolId);
}
