package com.school.erp.repository.auth;

import com.school.erp.entity.auth.UserAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserAssignmentRepository extends JpaRepository<UserAssignment, Long> {
    List<UserAssignment> findBySchoolIdAndUserIdAndIsActiveTrue(Long schoolId, Long userId);
    
    List<UserAssignment> findBySchoolIdAndUserIdAndAcademicSessionIdAndIsActiveTrue(Long schoolId, Long userId, Long academicSessionId);

    List<UserAssignment> findBySchoolIdAndUserIdAndAssignmentTypeAndIsActiveTrue(Long schoolId, Long userId, String assignmentType);

    boolean existsBySchoolIdAndUserIdAndAssignmentTypeAndClassIdAndSectionIdAndIsActiveTrue(Long schoolId, Long userId, String assignmentType, Long classId, Long sectionId);

    boolean existsBySchoolIdAndUserIdAndAssignmentTypeAndClassIdAndIsActiveTrue(Long schoolId, Long userId, String assignmentType, Long classId);

    boolean existsBySchoolIdAndUserIdAndAssignmentTypeAndClassIdAndSectionIdAndSubjectIdAndIsActiveTrue(Long schoolId, Long userId, String assignmentType, Long classId, Long sectionId, Long subjectId);

    boolean existsBySchoolIdAndUserIdAndAssignmentTypeAndClassIdAndSubjectIdAndIsActiveTrue(Long schoolId, Long userId, String assignmentType, Long classId, Long subjectId);
}

